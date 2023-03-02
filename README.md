Wrong "unresolved type" reported during native-image compilation: the actual missing type is something else.

## TL;DR:

```diff
-_I received_: ... Discovered unresolved type during parsing: org.acme.demo20230302.module1.MyClass2
+_I expected_: ... Discovered unresolved type during parsing: org.acme.demo20230302.module1.TraitB
```

## Details

A `module1` is in place of a generic library.

A `module2` depends on `module1`; module2 is in place of some consumer of said library.
In this case, module2 decides to shade only part of library module1 by shading it (license permits).
However by human mistake and omission one interface from module1 is forgotten to be included in the shaded jar of module2 while globbing module1 in itself.

A `module3` packages `module2` as a self-containing application.
As such, it does not depend on module1, just directly on module2.

`mvn clean install` (not native) _compiles_ without error.
Naturally, using the artifact from module3 may exhibit java.lang.ClassNotFoundException due to the omission above.

We come to the "unresolved type" reported during native-image compilation.

`mvn clean install -Pnative` to engage GraalVM native-image.

When compiling with native-image, the following error message is produced.

```
[INFO] --- native-maven-plugin:0.9.20:build (build-native) @ module3 ---
[WARNING] 'native:build' goal is deprecated. Use 'native:compile-no-fork' instead.
[INFO] Found GraalVM installation from GRAALVM_HOME variable.
[INFO] Executing: /Users/mmortari/.jabba/jdk/graalvm-ce-java17@22.3.1/Contents/Home/bin/native-image -cp /Users/mmortari/git/tmp/demo20230302-graalvmwrongerror/module3/target/module3-1.0-SNAPSHOT.jar:/Users/mmortari/git/tmp/demo20230302-graalvmwrongerror/module2/target/module2-1.0-SNAPSHOT.jar --no-fallback -H:Path=/Users/mmortari/git/tmp/demo20230302-graalvmwrongerror/module3/target -H:Name=module3 -H:Class=org.acme.demo20230302.module3.App3 --link-at-build-time
========================================================================================================================
GraalVM Native Image: Generating 'module3' (executable)...
========================================================================================================================
[1/7] Initializing...                                                                                    (4.7s @ 0.10GB)
 Version info: 'GraalVM 22.3.1 Java 17 CE'
 Java version info: '17.0.6+10-jvmci-22.3-b13'
 C compiler: cc (apple, x86_64, 14.0.0)
 Garbage collector: Serial GC
[2/7] Performing analysis...  []                                                                         (2.8s @ 0.33GB)
   1,394 (67.18%) of  2,075 classes reachable
   1,245 (42.81%) of  2,908 fields reachable
   5,049 (60.92%) of  8,288 methods reachable
      18 classes,     0 fields, and     0 methods registered for reflection

Fatal error: com.oracle.graal.pointsto.util.AnalysisError$ParsingError: Error encountered while parsing org.acme.demo20230302.module1.MyClass1$MySubClass1.a() 
Parsing context:
   at org.acme.demo20230302.module1.MyClass1$MySubClass1.a(MyClass1.java:24)
   at org.acme.demo20230302.module3.App3.asd(App3.java:15)
   at org.acme.demo20230302.module3.App3.main(App3.java:10)
   at com.oracle.svm.core.JavaMainWrapper.runCore0(JavaMainWrapper.java:175)
   at com.oracle.svm.core.JavaMainWrapper.runCore(JavaMainWrapper.java:135)

	at org.graalvm.nativeimage.pointsto/com.oracle.graal.pointsto.util.AnalysisError.parsingError(AnalysisError.java:153)
	at org.graalvm.nativeimage.pointsto/com.oracle.graal.pointsto.flow.MethodTypeFlow.createFlowsGraph(MethodTypeFlow.java:104)
	at org.graalvm.nativeimage.pointsto/com.oracle.graal.pointsto.flow.MethodTypeFlow.ensureFlowsGraphCreated(MethodTypeFlow.java:83)
	at org.graalvm.nativeimage.pointsto/com.oracle.graal.pointsto.flow.MethodTypeFlow.getOrCreateMethodFlowsGraph(MethodTypeFlow.java:65)
	at org.graalvm.nativeimage.pointsto/com.oracle.graal.pointsto.typestate.DefaultVirtualInvokeTypeFlow.onObservedUpdate(DefaultVirtualInvokeTypeFlow.java:109)
	at org.graalvm.nativeimage.pointsto/com.oracle.graal.pointsto.flow.TypeFlow.update(TypeFlow.java:562)
	at org.graalvm.nativeimage.pointsto/com.oracle.graal.pointsto.PointsToAnalysis$1.run(PointsToAnalysis.java:488)
	at org.graalvm.nativeimage.pointsto/com.oracle.graal.pointsto.util.CompletionExecutor.executeCommand(CompletionExecutor.java:193)
	at org.graalvm.nativeimage.pointsto/com.oracle.graal.pointsto.util.CompletionExecutor.lambda$executeService$0(CompletionExecutor.java:177)
	at java.base/java.util.concurrent.ForkJoinTask$RunnableExecuteAction.exec(ForkJoinTask.java:1395)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:373)
	at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1182)
	at java.base/java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1655)
	at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1622)
	at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:165)
Caused by: org.graalvm.compiler.java.BytecodeParser$BytecodeParserError: com.oracle.graal.pointsto.constraints.UnresolvedElementException: Discovered unresolved type during parsing: org.acme.demo20230302.module1.MyClass2. This error is reported at image build time because class org.acme.demo20230302.module1.MyClass1$MySubClass1 is registered for linking at image build time by command line
	at parsing org.acme.demo20230302.module1.MyClass1$MySubClass1.a(MyClass1.java:24)
	at jdk.internal.vm.compiler/org.graalvm.compiler.java.BytecodeParser.throwParserError(BytecodeParser.java:2518)
	at org.graalvm.nativeimage.builder/com.oracle.svm.hosted.phases.SharedGraphBuilderPhase$SharedBytecodeParser.throwParserError(SharedGraphBuilderPhase.java:110)
	at jdk.internal.vm.compiler/org.graalvm.compiler.java.BytecodeParser.iterateBytecodesForBlock(BytecodeParser.java:3393)
	at jdk.internal.vm.compiler/org.graalvm.compiler.java.BytecodeParser.handleBytecodeBlock(BytecodeParser.java:3345)
	at jdk.internal.vm.compiler/org.graalvm.compiler.java.BytecodeParser.processBlock(BytecodeParser.java:3190)
	at jdk.internal.vm.compiler/org.graalvm.compiler.java.BytecodeParser.build(BytecodeParser.java:1138)
	at jdk.internal.vm.compiler/org.graalvm.compiler.java.BytecodeParser.buildRootMethod(BytecodeParser.java:1030)
	at jdk.internal.vm.compiler/org.graalvm.compiler.java.GraphBuilderPhase$Instance.run(GraphBuilderPhase.java:97)
	at org.graalvm.nativeimage.builder/com.oracle.svm.hosted.phases.SharedGraphBuilderPhase.run(SharedGraphBuilderPhase.java:84)
	at jdk.internal.vm.compiler/org.graalvm.compiler.phases.Phase.run(Phase.java:49)
	at jdk.internal.vm.compiler/org.graalvm.compiler.phases.BasePhase.apply(BasePhase.java:446)
	at jdk.internal.vm.compiler/org.graalvm.compiler.phases.Phase.apply(Phase.java:42)
	at jdk.internal.vm.compiler/org.graalvm.compiler.phases.Phase.apply(Phase.java:38)
	at org.graalvm.nativeimage.pointsto/com.oracle.graal.pointsto.flow.AnalysisParsedGraph.parseBytecode(AnalysisParsedGraph.java:135)
	at org.graalvm.nativeimage.pointsto/com.oracle.graal.pointsto.meta.AnalysisMethod.ensureGraphParsed(AnalysisMethod.java:685)
	at org.graalvm.nativeimage.pointsto/com.oracle.graal.pointsto.flow.MethodTypeFlowBuilder.parse(MethodTypeFlowBuilder.java:171)
	at org.graalvm.nativeimage.pointsto/com.oracle.graal.pointsto.flow.MethodTypeFlowBuilder.apply(MethodTypeFlowBuilder.java:349)
	at org.graalvm.nativeimage.pointsto/com.oracle.graal.pointsto.flow.MethodTypeFlow.createFlowsGraph(MethodTypeFlow.java:93)
	... 13 more
Caused by: com.oracle.graal.pointsto.constraints.UnresolvedElementException: Discovered unresolved type during parsing: org.acme.demo20230302.module1.MyClass2. This error is reported at image build time because class org.acme.demo20230302.module1.MyClass1$MySubClass1 is registered for linking at image build time by command line
	at org.graalvm.nativeimage.builder/com.oracle.svm.hosted.phases.SharedGraphBuilderPhase$SharedBytecodeParser.reportUnresolvedElement(SharedGraphBuilderPhase.java:333)
	at org.graalvm.nativeimage.builder/com.oracle.svm.hosted.phases.SharedGraphBuilderPhase$SharedBytecodeParser.handleUnresolvedType(SharedGraphBuilderPhase.java:288)
	at org.graalvm.nativeimage.builder/com.oracle.svm.hosted.phases.SharedGraphBuilderPhase$SharedBytecodeParser.handleUnresolvedNewInstance(SharedGraphBuilderPhase.java:204)
	at jdk.internal.vm.compiler/org.graalvm.compiler.java.BytecodeParser.genNewInstance(BytecodeParser.java:4501)
	at jdk.internal.vm.compiler/org.graalvm.compiler.java.BytecodeParser.genNewInstance(BytecodeParser.java:4494)
	at jdk.internal.vm.compiler/org.graalvm.compiler.java.BytecodeParser.processBytecode(BytecodeParser.java:5291)
	at jdk.internal.vm.compiler/org.graalvm.compiler.java.BytecodeParser.iterateBytecodesForBlock(BytecodeParser.java:3385)
	... 28 more
------------------------------------------------------------------------------------------------------------------------
                        0.2s (2.7% of total time) in 11 GCs | Peak RSS: 0.90GB | CPU load: 5.17
========================================================================================================================
```

Please note the error message (snippet):

```
... unresolved type during parsing: org.acme.demo20230302.module1.MyClass2 ...
```

However, according to content of Jar of module2:

```
$ jar tvf module2/target/module2-1.0-SNAPSHOT.jar 
     0 Thu Mar 02 17:03:42 CET 2023 META-INF/
    96 Thu Mar 02 17:03:42 CET 2023 META-INF/MANIFEST.MF
     0 Thu Mar 02 17:03:40 CET 2023 org/
     0 Thu Mar 02 17:03:40 CET 2023 org/acme/
     0 Thu Mar 02 17:03:40 CET 2023 org/acme/demo20230302/
     0 Thu Mar 02 17:03:40 CET 2023 org/acme/demo20230302/module2/
   652 Thu Mar 02 17:03:40 CET 2023 org/acme/demo20230302/module2/App2.class
     0 Thu Mar 02 16:19:04 CET 2023 META-INF/maven/
     0 Thu Mar 02 16:19:04 CET 2023 META-INF/maven/org.acme.demo20230302/
     0 Thu Mar 02 16:19:04 CET 2023 META-INF/maven/org.acme.demo20230302/module2/
  2305 Thu Mar 02 16:19:04 CET 2023 META-INF/maven/org.acme.demo20230302/module2/pom.xml
   101 Thu Mar 02 17:03:42 CET 2023 META-INF/maven/org.acme.demo20230302/module2/pom.properties
     0 Thu Mar 02 17:03:40 CET 2023 org/acme/demo20230302/module1/
  1182 Thu Mar 02 17:03:40 CET 2023 org/acme/demo20230302/module1/MyClass1$MySubClass1.class
   464 Thu Mar 02 17:03:40 CET 2023 org/acme/demo20230302/module1/MyClass2.class
   158 Thu Mar 02 17:03:40 CET 2023 org/acme/demo20230302/module1/TraitA.class
   702 Thu Mar 02 17:03:40 CET 2023 org/acme/demo20230302/module1/MyClass1.class
```

What is actually missing, is `TraitB.class` which is used in the definition of `MyClass2.class`.

