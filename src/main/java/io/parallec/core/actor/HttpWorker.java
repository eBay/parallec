/*  
Copyright [2013-2015] eBay Software Foundation
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package io.parallec.core.actor;

import io.parallec.core.actor.message.ResponseOnSingeRequest;
import io.parallec.core.actor.message.type.RequestWorkerMsgType;
import io.parallec.core.exception.ActorMessageTypeInvalidException;
import io.parallec.core.exception.HttpRequestCreateException;
import io.parallec.core.resources.HttpMethod;
import io.parallec.core.util.PcConstants;
import io.parallec.core.util.PcDateUtils;
import io.parallec.core.util.PcErrorMsgUtils;
import io.parallec.core.util.PcHttpUtils;
import io.parallec.core.util.PcStringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
//import akka.actor.PoisonPill;
import akka.actor.UntypedActor;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;


/**
 * This is an akka actor with async http client.
 *
 * @author Yuanteng Jeff Pei
 */
public class HttpWorker extends UntypedActor {

    private int actorMaxOperationTimeoutSec;

    /** The client. */
    private final AsyncHttpClient client;

    /** The request url. */
    private final String requestUrl;

    /** The http method. */
    private final HttpMethod httpMethod;

    /** The post data. */
    private final String postData;

    /** The logger. */
    private static Logger logger = LoggerFactory.getLogger(HttpWorker.class);

    /** The http header map. */
    // 20140310
    private final Map<String, String> httpHeaderMap = new HashMap<String, String>();

    /** The sender. */
    private ActorRef sender = null;

    /** The cause. */
    // cause will be the exception to log as in PROCESS_ON_EXCEPTION
    private Throwable cause;

    /** The try count. */
    private int tryCount = 0;

    /** The timeout message cancellable. */
    private Cancellable timeoutMessageCancellable = null;

    /** The timeout duration. */
    private FiniteDuration timeoutDuration = null;

    /** The sent reply. */
    // private FiniteDuration retryDuration = null;
    private boolean sentReply = false;

    /** The response future. */
    // 20150221
    ListenableFuture<ResponseOnSingeRequest> responseFuture = null;

    /**
     * Instantiates a new http worker.
     *
     * @param client
     *            the client
     * @param requestUrl
     *            the request url
     * @param httpMethod
     *            the http method
     * @param postData
     *            the post data
     * @param httpHeaderMap
     *            the http header map
     */
    public HttpWorker(final int actorMaxOperationTimeoutSec,
            final AsyncHttpClient client, final String requestUrl,
            final HttpMethod httpMethod, final String postData,
            final Map<String, String> httpHeaderMap

    ) {
        this.actorMaxOperationTimeoutSec = actorMaxOperationTimeoutSec;
        this.client = client;
        this.requestUrl = requestUrl;
        this.httpMethod = httpMethod;
        this.postData = postData;
        if (httpHeaderMap != null)
            this.httpHeaderMap.putAll(httpHeaderMap);

    }

    /**
     * Creates the request.
     *
     * @return the bound request builder
     * @throws HttpRequestCreateException
     *             the http request create exception
     */
    public BoundRequestBuilder createRequest()
            throws HttpRequestCreateException {
        BoundRequestBuilder builder = null;

        getLogger().debug("AHC completeUrl " + requestUrl);

        try {

            switch (httpMethod) {
            case GET:
                builder = client.prepareGet(requestUrl);
                break;
            case POST:
                builder = client.preparePost(requestUrl);
                break;
            case PUT:
                builder = client.preparePut(requestUrl);
                break;
            case HEAD:
                builder = client.prepareHead(requestUrl);
                break;
            case OPTIONS:
                builder = client.prepareOptions(requestUrl);
                break;
            case DELETE:
                builder = client.prepareDelete(requestUrl);
                break;
            default:
                break;
            }

            PcHttpUtils.addHeaders(builder, this.httpHeaderMap);
            if (!PcStringUtils.isNullOrEmpty(postData)) {
                builder.setBody(postData);
            }

        } catch (Exception t) {
            throw new HttpRequestCreateException(
                    "Error in creating request in Httpworker. "
                            + " If BoundRequestBuilder is null. Then fail to create.",
                    t);
        }

        return builder;

    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    @Override
    public void onReceive(Object message) throws Exception {
        try {
            if (message instanceof RequestWorkerMsgType) {
                switch ((RequestWorkerMsgType) message) {
                case PROCESS_REQUEST:
                    tryCount++;

                    if (tryCount == 1) {
                        sender = getSender();

                        BoundRequestBuilder request = createRequest();

                        // 20150229: create the future and make sure future is
                        // killed when timeout.
                        responseFuture = request.execute(new HttpAsyncHandler(
                                this));
                        timeoutDuration = Duration.create(
                                actorMaxOperationTimeoutSec, TimeUnit.SECONDS);

                        // To handle cases where nio response never comes back,
                        // we
                        // schedule a 'timeout' message to be sent to us 2
                        // seconds
                        // after NIO's SO_TIMEOUT
                        timeoutMessageCancellable = getContext()
                                .system()
                                .scheduler()
                                .scheduleOnce(
                                        timeoutDuration,
                                        getSelf(),
                                        RequestWorkerMsgType.PROCESS_ON_TIMEOUT,
                                        getContext().system().dispatcher(),
                                        getSelf());
                    } else {
                        getLogger().error(
                                "duplicated PROCESS_REQUEST msg. ignore...");
                    }
                    break;

                case CANCEL:
                    getLogger().info(
                            "Request was CANCELLED.................{}",
                            requestUrl);
                    cancelCancellable();
                    if (sender == null)
                        sender = getSender();
                    reply(null, true, PcConstants.REQUEST_CANCELED,
                            PcConstants.REQUEST_CANCELED, PcConstants.NA,
                            PcConstants.NA_INT);
                    break;

                case PROCESS_ON_EXCEPTION:

                    String errorSummary = PcErrorMsgUtils.replaceErrorMsg(cause
                            .toString());
                    String stackTrace = PcStringUtils.printStackTrace(cause);
                    cancelCancellable();
                    reply(null, true, errorSummary, stackTrace, PcConstants.NA,
                            PcConstants.NA_INT);

                    break;

                case PROCESS_ON_TIMEOUT:

                    getLogger().error("PROCESS_ON_TIMEOUT.................{}",
                            requestUrl);
                    cancelCancellable();

                    String errorMsg = String
                            .format("HttpWorker Timedout after %d SEC (no response but no exception catched). Check URL: may be very slow or stuck.",
                                    actorMaxOperationTimeoutSec);

                    reply(null, true, errorMsg, errorMsg, PcConstants.NA,
                            PcConstants.NA_INT);
                    break;

                case CHECK_FUTURE_STATE:
                default:
                    this.cause = new ActorMessageTypeInvalidException(
                            "ActorMessageTypeInvalidException error for url "
                                    + this.requestUrl);
                    getSelf().tell(RequestWorkerMsgType.PROCESS_ON_EXCEPTION,
                            getSelf());
                    break;
                }
            } else {
                unhandled(message);
                this.cause = new ActorMessageTypeInvalidException(
                        "ActorMessageTypeInvalidException error for url "
                                + this.requestUrl);
                getSelf().tell(RequestWorkerMsgType.PROCESS_ON_EXCEPTION,
                        getSelf());
            }
        } catch (Exception e) {
            this.cause = e;
            getSelf()
                    .tell(RequestWorkerMsgType.PROCESS_ON_EXCEPTION, getSelf());
        }
    }

    /**
     */
    public void cancelCancellable() {

        // responseFuture: to cancel the future.boolean cancelResponseFuture
        // if (cancelResponseFuture && responseFuture != null &&
        // !responseFuture.isDone()) {
        // responseFuture.cancel(true);
        // }

        if (timeoutMessageCancellable != null) {
            timeoutMessageCancellable.cancel();
        }

    }

    /**
     * Reply.
     *
     * @param response
     *            the response
     * @param error
     *            the error
     * @param errorMessage
     *            the error message
     * @param stackTrace
     *            the stack trace
     * @param statusCode
     *            the status code
     * @param statusCodeInt
     *            the status code int
     */
    private void reply(final String response, final boolean error,
            final String errorMessage, final String stackTrace,
            final String statusCode, final int statusCodeInt) {

        if (!sentReply) {
            //must update sentReply first to avoid duplicated msg.
            sentReply = true;
            final ResponseOnSingeRequest res = new ResponseOnSingeRequest(
                    response, error, errorMessage, stackTrace, statusCode,
                    statusCodeInt, PcDateUtils.getNowDateTimeStrStandard());
            if (!getContext().system().deadLetters().equals(sender)) {
                sender.tell(res, getSelf());
            }
            
            getContext().stop(getSelf());
        }

    }

    /**
     * On complete.
     *
     * @param response
     *            the response
     * @return the response on singe request
     */
    public ResponseOnSingeRequest onComplete(Response response) {

        cancelCancellable();
        try {

            int statusCodeInt = response.getStatusCode();
            String statusCode = statusCodeInt + " " + response.getStatusText();

            reply(response.getResponseBody(), false, null, null, statusCode,
                    statusCodeInt);
        } catch (IOException e) {
            getLogger().error("fail response.getResponseBody " + e);
        }

        return null;
    }

    /**
     * On throwable.
     *
     * @param cause
     *            the cause
     */
    public void onThrowable(Throwable cause) {
        this.cause = cause;
        getSelf().tell(RequestWorkerMsgType.PROCESS_ON_EXCEPTION, getSelf());

    }

    /**
     * Gets the logger.
     *
     * @return the logger
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Sets the logger.
     *
     * @param logger
     *            the new logger
     */
    public static void setLogger(Logger logger) {
        HttpWorker.logger = logger;
    }

    /**
     * Async HTTP Client handler wrapper.
     */
    static class HttpAsyncHandler extends
            AsyncCompletionHandler<ResponseOnSingeRequest> {

        /** The http worker. */
        private final HttpWorker httpWorker;

        /**
         * Instantiates a new http async handler.
         *
         * @param httpWorker
         *            the http worker
         */
        public HttpAsyncHandler(HttpWorker httpWorker) {
            this.httpWorker = httpWorker;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.ning.http.client.AsyncCompletionHandler#onCompleted(com.ning.
         * http.client.Response)
         */
        @Override
        public ResponseOnSingeRequest onCompleted(Response response)
                throws Exception {

            ResponseOnSingeRequest myResponse = null;
            httpWorker.onComplete(response);
            return myResponse;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.ning.http.client.AsyncCompletionHandler#onThrowable(java.lang
         * .Throwable)
         */
        @Override
        public void onThrowable(Throwable t) {
            httpWorker.onThrowable(t);
        }
    }// end class

}
