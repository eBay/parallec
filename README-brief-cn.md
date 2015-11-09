[Parallec](http://www.parallec.io/) 是一个快速的并行异步HTTP(S)/SSH/TCP/Ping 客户端的Java库。它是eBay云计算部门开发并在[REST Commander](http://www.restcommander.com/)基础上开源的。Parallec的寓意是**"并行客户端"**： **Paralle**l **C**lient, 发音如 "Para-like". 更多说明请见[这里](http://www.parallec.io/)。

其内置特别的响应上下文（response context），在处理服务器回复（Response）时能方便快捷的传入，传出任何对象，比如各种客户端（比如elastic search, kafka, mongodb etc），以便汇集处理数据到任何地方。 [样例程序](http://www.parallec.io/#code-sample)**仅需20行代码**，就可以汇总10000隔API的回复以简单可控的并行速度发送汇总到Elastic Search。

和REST Commander类似，Parallec非常高效并且可以处理大量响应。 比如其Ping服务器的速度是目前使用最广泛**并行Ping 软件[FPing](http://fping.org/)的2倍**，仅用12秒就可以ping 8000服务器。（[**请看视频演示**](https://github.com/eBay/parallec/wiki/Parallec-pings-8000-servers-in-11.1-seconds))

通过过去一年内部使用和开源REST Superman/ REST Commander的开发， 反馈，经验教训，我们现在做把REST Commander的核心做成一个易于使用的独立库 （便于从Maven导入，让所有程序都写20行代码变成REST Commander）。在此基础上，我们添加超过[**15个新的功能**](https://github.com/eBay/parallec/blob/master/README.md#compare)，**改写超过70%的代码**，**以90%以上的测试覆盖率**，确保了用户和开源贡献者能够更加放心的开发和使用。与此同时，我们也把整体软件结构进行了优化，使得大部分内部开发可以直接在github开源平台上直接进行。

您可以用5分钟按照[样例程序](http://www.parallec.io/#code-sample)试试Parallec去给多个服务器（网站）发送相同或者不同的请求。我们渴望收到您的意见和建议。您可以通过[提交Github Issue](https://github.com/eBay/parallec/issues/new)进行反馈。谢谢！
