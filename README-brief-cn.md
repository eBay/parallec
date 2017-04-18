
<a href="http://www.parallec.io"><img alt="Parallec-logo" src="http://www.parallec.io/images/parallec-logo.png" width="325"></a>

## Parallec 中文介绍

[Parallec](http://www.parallec.io/) 是一个基于[Akka](http://akka.io)的快速并行异步HTTP(S)/SSH/TCP/UDP/Ping 客户端的Java库。它是eBay云计算部门开发并在[REST Commander](http://www.restcommander.com/)基础上开源的。Parallec的寓意是**"并行客户端"**： **Paralle**l **C**lient, 发音如 "Para-like". 更多说明请见[这里](http://www.parallec.io/)。

主要用途： 

- 管理监控大量HTTP/TCP/UDP服务器，ping海量服务器
- 集群机器状态、配置探索，基于HTTP(S)/TCP/UDP Agent 或者 无agent 的大量远程任务执行，软件部署, 网络监控. HTTP支持SSL 客户端认证
- 海量API聚集到任意数据存储，数据流入口
- 并行工作流，自动检测任务进度，以便支持并行处理异步API
- 可控并行的，多个不同API请求到同一个HTTP服务器。HTTP请求模板可以在URL, HTTP Header, EntityBody不同地方进行变量替换。这样可以很方便的对那些有REST API的数据库或者网络服务器进行并行的CRUD操作。
- 灵活的Web服务器的压力测试，并将结果传到任何地方。 

Parallec内置特别的响应上下文（response context），在处理服务器回复（Response）时能方便快捷的传入，传出任何对象，比如各种客户端（比如elastic search, graphite, kafka, mongodb etc），以便汇集处理数据到任何地方。 [样例程序](http://www.parallec.io/#code-sample)**仅需20行代码**，就可以汇总10000个API的回复以简单可控的并行速度发送汇总到Elastic Search。

和REST Commander类似，Parallec非常高效并且可以处理大量响应。 比如其Ping服务器的速度是目前使用最广泛**并行Ping 软件[FPing](http://fping.org/)的2倍**，仅用12秒就可以ping 8000服务器。（[**请看Ping视频演示**](https://github.com/eBay/parallec/wiki/Parallec-pings-8000-servers-in-11.1-seconds)) 同样的对于**并行HTTP**， Parallec发送HTTP请求到8000 Web服务器 并聚集响应只需**12秒** （聚集到内存），或**16秒**（聚集到Elastic Search）（[**请看HTTP视频演示**](https://github.com/eBay/parallec/wiki/Parallec-Aggregates-HTTP-Responses-from-8000-Servers))

通过过去一年内部使用和开源REST Superman/ REST Commander的开发， 反馈，经验教训，我们现在做把REST Commander的核心做成一个易于使用的独立库 （便于从Maven导入，让所有程序都写20行代码变成REST Commander）。在此基础上，我们添加超过[**15个新的功能**](https://github.com/eBay/parallec/blob/master/README.md#compare)，**改写超过70%的代码**，**以90%以上的测试覆盖率**，确保了用户和开源贡献者能够更加放心的开发和使用。与此同时，我们也把整体软件结构进行了优化，使得大部分内部开发可以直接在github开源平台上直接进行。

**生产环境使用案例**：广泛用于基础设施软件作为轮询和聚合引擎:

* 应用程序部署/ PaaS：Parallec已集成在eBay主要生产应用程序部署系统（PaaS）中。 Parallec协调10+个API任务，每**个任务针对生产中超过1,000个应用程序池的10到1,000个服务器。 Parallec已经与工作流引擎Winder一起使用来处理复杂的部署工作流程。
* 数据提取/ ETL：Parallec已被eBay以色列的结构化大数据团队用于对单个第三方服务器执行10k-100k API并行调用，具有显着改进的性能和减少的资源。
* 主动探测进行网络故障排除：在eBay的网络/云团队中，Parallec有助于确保极低的错误警报率，从而准确检测交换机软故障。 Parallec在Master组件中充当核心轮询引擎，以检查Agent运行状况，及时有效地消除噪音和错误报警。
* Agent管理/Agent Master：在eBay的网站操作/工具团队中，Parallec作为核心引擎，在100,000个生产服务器上管理和监控一种和 puppet agent / salt minion / kubernetes kubelet 类似的agent，以确保快速大规模的并行的操作。

“Parallec.io现已经被集成在eBay的应用程序部署系统(PaaS)。Parallec被用于控制10个以上的并行API的任务的工作流，每个任务并行执行到数十到数千台的生产服务器的上。与以前的实现相比，Parallec 处理并行操作代码简洁，方便进行批作业的管理，为我们提高了资源利用率和效率。”
－－ Xiaotang (Sheldon) Shao, Principle Software Engineer, Deployment Platform / PaaS, Cloud Engineering at eBay Inc.

“eBay 网络智能分析组目前的几个项目都需要并行执行万级到十万级的API到第三方的服务器，这毫无疑问是我们最大的瓶颈。因此我们一致在寻找一个高效的异步I/O的解决方案。使用Parallec，不但显著的提升了性能，同时有效的减少了资源的使用。 ” －－ Tomer Balan, Data Extraction Engineer, Structured Data at eBay Inc (以色列).

您可以用5分钟按照[样例程序](http://www.parallec.io/#code-sample)试试Parallec去给多个服务器（网站）发送相同或者不同的请求。我们渴望收到您的意见和建议。您可以通过[提交Github Issue](https://github.com/eBay/parallec/issues/new), 查看[FAQ](http://www.parallec.io/docs/faq/)，登陆论坛 [Parallec.io Google Group](https://groups.google.com/forum/#!forum/parallec)进行反馈或者提问。谢谢！

