package io.parallec.core.main.http.pollable.sampleserver;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

/**
 * runs at port 10080 will run forever until being killed
 * 
 * Accept 1. POST: /submitJob; 2. GET: /status/$JOB_ID ; 3. GET /testHeaders to
 * log "sample" keyvalue in header
 * 
 * note that the job map will never be clean up until it reaches 8192
 * 
 * @author Yuanteng (Jeff) Pei
 *
 */
public class ServerWithPollableJobs extends NanoHTTPD {

    public static String AUTH_KEY = "SAMPLE_AUTH_KEY";
    private static Logger logger = Logger
            .getLogger(ServerWithPollableJobs.class.getName());
    public static final int PORT = 10080;

    public ServerWithPollableJobs() throws IOException {
        super(PORT);
        logger.info("Try to start nano server. make sure port " + PORT
                + " is not used!");
        start();

        logger.info("\nWeb Server with Pollable Jobs Running! \n"
                + "Accept 1. POST: /submitJob; 2. GET: /status/$JOB_ID s\n"
                + "Point your browers to http://localhost:10080/ \n");
    }

    public static void main(String[] args) {
        try {
            new ServerWithPollableJobs();
            while (true) {
                ;
            }
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }

    public static Map<String, NanoJob> jobMap = new ConcurrentHashMap<String, NanoJob>();

    public final int MAX_JOB_SIZE = 8192;

    public synchronized NanoJob addJob() {

        if (jobMap.size() >= MAX_JOB_SIZE) {
            logger.info("jobMap too large. clean up");
            jobMap.clear();
        }

        NanoJob job = new NanoJob();
        jobMap.put(job.getJobId(), job);

        return job;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String msg = "<html><body><h1>Hello server</h1>\n";
        String msgUnauthorized = "<html><body><h1>Unauthorized</h1>\n";
        Method method = session.getMethod();
        String uri = session.getUri();

        /**
         * if request is submit a job: 1. submit job POST: /submitJob return:
         * {"status": "/status/01218499-a5fe-47cf-a0a8-8e9b106c5219",
         * "progress": 0}
         * 
         * 2. poll progress GET: /status/{JobID}
         * 
         * 
         */

        logger.info("GET REQ: Method " + method + "  URL: " + uri);

        if (method == Method.POST && uri.contains("submitJob")) {

            // careful authorization may change to lower case
            if ((session.getHeaders().containsKey("authorization") && session
                    .getHeaders().get("authorization")
                    .equalsIgnoreCase(AUTH_KEY))) {

                NanoJob job = addJob();

                msg = "{\"status\": \"/status/" + job.getJobId()
                        + "\", \"progress\": 0}";
                logger.info("SERVER_RESPONSE_200: " + msg);
                return new fi.iki.elonen.NanoHTTPD.Response(Status.OK,
                        "application/json", msg);
            } else {
                logger.info("SERVER_RESPONSE_401: " + msg);
                return new fi.iki.elonen.NanoHTTPD.Response(
                        Status.UNAUTHORIZED, "application/json",
                        msgUnauthorized);
            }

        } else if (method == Method.GET && uri.contains("/status/")) {

            // poll progress
            String jobId = uri.replace("/status/", "");
            NanoJob pollJob = jobMap.get(jobId);
            if (pollJob == null) {
                msg = "{\"status\": \"/status/" + jobId
                        + "\", \"errorMsg\": \"job not found\"}";
                logger.info("SERVER_RESPONSE_404: " + msg);
                return new fi.iki.elonen.NanoHTTPD.Response(Status.NOT_FOUND,
                        "application/json", msg);
            } else {
                pollJob.makeProgress();
                msg = "{\"status\": \"/status/" + jobId + "\", \"progress\": "
                        + pollJob.getProgress() + "}";
                logger.info("SERVER_RESPONSE_200: " + msg);
                return new fi.iki.elonen.NanoHTTPD.Response(Status.OK,
                        "application/json", msg);
            }

        } else if (method == Method.GET && uri.contains("/testHeaders")) {

            if (session.getHeaders().containsKey("sample")) {
                logger.info("Sample Header value {}: "
                        + session.getHeaders().get("sample"));

                msg = session.getHeaders().get("sample");
                logger.info("SERVER_RESPONSE_200: " + msg);
                return new fi.iki.elonen.NanoHTTPD.Response(Status.OK,
                        "application/json", msg);

            }

        }// end else
        msg = "{ \"errorMsg\": \"bad request. Accept 1. POST: /submitJob; 2. GET: /status/$JOB_ID \"}";
        return new fi.iki.elonen.NanoHTTPD.Response(Status.BAD_REQUEST,
                "application/json", msg);

    }

    public static class NanoJob {

        private String jobId;
        private int progress;

        private Date startTime;

        public void makeProgress() {

            Date currentTime = new Date();
            int seconds = (int) ((currentTime.getTime() - startTime.getTime()) / 1000L);

            if (seconds >= 10)
                progress = 100;
            else
                progress = (int) seconds * 10;

        }

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public NanoJob(String jobId, int progress) {
            super();
            this.jobId = jobId;
            this.progress = progress;
            this.startTime = new Date();
        }

        public NanoJob() {
            super();
            this.jobId = UUID.randomUUID().toString();
            this.progress = 0;
            this.startTime = new Date();
        }
    }

}
