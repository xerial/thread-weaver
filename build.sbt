import Settings._
import Utils.RandomPortSupport
import com.typesafe.sbt.packager.docker._
import org.seasar.util.lang.StringUtil

import scala.concurrent.duration._

val dbDriver   = "com.mysql.jdbc.Driver"
val dbName     = "tw"
val dbUser     = "tw"
val dbPassword = "passwd"
val dbPort     = RandomPortSupport.temporaryServerPort()
val dbUrl      = s"jdbc:mysql://localhost:$dbPort/$dbName?useSSL=false"

val `infrastructure` = (project in file("modules/infrastructure"))
  .settings(baseSettings)
  .settings(
    name := "thread-weaver-infrastructure",
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "de.huxhorn.sulky" % "de.huxhorn.sulky.ulid" % "8.2.0",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.github.julien-truffaut" %% "monocle-core" % monocleVersion,
      "com.github.julien-truffaut" %% "monocle-macro" % monocleVersion,
      "com.google.guava" % "guava" % "27.1-jre"
    )
  )

val `domain` = (project in file("modules/domain"))
  .settings(baseSettings)
  .settings(
    name := "thread-weaver-domain"
  ).dependsOn(`infrastructure`)


val `contract-use-case` = (project in file("contracts/contract-use-case"))
  .settings(baseSettings)
  .settings(
    name := "thread-weaver-contract-use-case",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
    )
  )
  .dependsOn(`domain`)

lazy val `grpc-proto` = (project in file("modules/grpc-proto"))
  .enablePlugins(AkkaGrpcPlugin)
  .settings(
  )

val `contract-interface` = (project in file("contracts/contract-interface"))
  .settings(baseSettings)
  .settings(
    name := "thread-weaver-contract-interface",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "javax.ws.rs" % "javax.ws.rs-api" % "2.0.1",
      "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.0.2",
      "com.github.swagger-akka-http" %% "swagger-scala-module" % "2.0.3",
      "io.swagger.core.v3" % "swagger-core" % swaggerVersion,
      "io.swagger.core.v3" % "swagger-annotations" % swaggerVersion,
      "io.swagger.core.v3" % "swagger-models" % swaggerVersion,
      "io.swagger.core.v3" % "swagger-jaxrs2" % swaggerVersion
    )
  )
  .dependsOn(`contract-use-case`, `grpc-proto`)

val `use-case` = (project in file("modules/use-case"))
  .settings(baseSettings)
  .settings(
    name := "thread-weaver-use-case",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion
    )
  ).dependsOn(`contract-use-case`, `contract-interface`, `domain`, `infrastructure`)

val `flyway` = (project in file("tools/flyway"))
  .enablePlugins(FlywayPlugin)
  .settings(baseSettings)
  .settings(
    name := "thread-weaver-flyway",
    libraryDependencies ++= Seq("mysql" % "mysql-connector-java" % "5.1.42"),
    parallelExecution in Test := false,
    wixMySQLVersion := com.wix.mysql.distribution.Version.v5_6_21,
    wixMySQLUserName := Some(dbUser),
    wixMySQLPassword := Some(dbPassword),
    wixMySQLSchemaName := dbName,
    wixMySQLPort := Some(dbPort),
    wixMySQLDownloadPath := Some(sys.env("HOME") + "/.wixMySQL/downloads"),
    wixMySQLTimeout := Some(2 minutes),
    flywayDriver := dbDriver,
    flywayUrl := dbUrl,
    flywayUser := dbUser,
    flywayPassword := dbPassword,
    flywaySchemas := Seq(dbName),
    flywayLocations := Seq(
      s"filesystem:${baseDirectory.value}/src/test/resources/db-migration/",
      s"filesystem:${baseDirectory.value}/src/test/resources/db-migration/test"
    ),
    flywayPlaceholderReplacement := true,
    flywayPlaceholders := Map(
      "engineName"                 -> "MEMORY",
      "idSequenceNumberEngineName" -> "MyISAM"
    ),
    flywayMigrate := (flywayMigrate dependsOn wixMySQLStart).value
  )

lazy val `api-client` = (project in file("api-client"))
  .dependsOn(`grpc-proto`)
  .enablePlugins(AkkaGrpcPlugin)

val interface = (project in file("modules/interface"))
  .enablePlugins(AkkaGrpcPlugin)
  .settings(baseSettings)
  .settings(
    name := "thread-weaver-interface",
    libraryDependencies ++= Seq(
      "mysql" % "mysql-connector-java" % "5.1.42",
      "com.typesafe.slick" %% "slick"          % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "de.heikoseeberger" %% "akka-http-circe" % "1.25.2",
      "ch.megard" %% "akka-http-cors" % "0.4.0",
      "com.github.j5ik2o" %% "akka-persistence-dynamodb" % "1.0.2",
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "com.github.julien-truffaut" %% "monocle-law" % monocleVersion % Test,
      "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8" % Test,
      "org.iq80.leveldb" % "leveldb" % "0.9" % Test,
      "commons-io" % "commons-io" % "2.4" % Test,
      "com.github.j5ik2o" %% "reactive-aws-dynamodb-core" % "1.1.0" % Test,
      "com.github.j5ik2o" %% "reactive-aws-dynamodb-test" % "1.1.0" % Test,
      "com.github.j5ik2o" %% "scalatestplus-db" % "1.0.8" % Test

    ),
    // sbt-dao-generator
    // JDBCのドライバークラス名を指定します(必須)
    driverClassName in generator := dbDriver,
    // JDBCの接続URLを指定します(必須)
    jdbcUrl in generator := dbUrl,
    // JDBCの接続ユーザ名を指定します(必須)
    jdbcUser in generator := dbUser,
    // JDBCの接続ユーザのパスワードを指定します(必須)
    jdbcPassword in generator := dbPassword,
    // カラム型名をどのクラスにマッピングするかを決める関数を記述します(必須)
    propertyTypeNameMapper in generator := {
      case "INTEGER" | "TINYINT" | "INT"     => "Int"
      case "BIGINT"                          => "Long"
      case "VARCHAR"                         => "String"
      case "BOOLEAN" | "BIT"                 => "Boolean"
      case "DATE" | "TIMESTAMP" | "DATETIME" => "java.time.Instant"
      case "DECIMAL"                         => "BigDecimal"
      case "ENUM"                            => "String"
    },
    propertyNameMapper in generator := {
      case "type"     => "`type`"
      case columnName => StringUtil.decapitalize(StringUtil.camelize(columnName))
    },
    tableNameFilter in generator := { tableName: String =>
      tableName.toUpperCase match {
        case "SCHEMA_VERSION"                      => false
        case "FLYWAY_SCHEMA_HISTORY"                => false
        case t if t.endsWith("ID_SEQUENCE_NUMBER") => false
        case _                                     => true
      }
    },
    outputDirectoryMapper in generator := {
      case s if s.endsWith("Spec") => (sourceDirectory in Test).value
      case s =>
        new java.io.File((scalaSource in Compile).value, "/com/github/j5ik2o/threadWeaver/adaptor/dao/jdbc")
    },
    // モデル名に対してどのテンプレートを利用するか指定できます。
    templateNameMapper in generator := {
      case className if className.endsWith("Spec") => "template_spec.ftl"
      case _                                       => "template.ftl"
    },
    compile in Compile := ((compile in Compile) dependsOn (generateAll in generator)).value,
    generateAll in generator := Def
      .taskDyn {
        val ga = (generateAll in generator).value
        Def
          .task {
            (wixMySQLStop in flyway).value
          }
          .map(_ => ga)
      }
      .dependsOn(flywayMigrate in flyway)
      .value,
    // --- sbt-multi-jvm用の設定
    compile in MultiJvm := (compile in MultiJvm).triggeredBy(compile in Test).value,
    executeTests in Test := Def.task {
      val testResults = (executeTests in Test).value
      val multiNodeResults = (executeTests in MultiJvm).value
      val overall = (testResults.overall, multiNodeResults.overall) match {
        case (TestResult.Passed, TestResult.Passed) => TestResult.Passed
        case (TestResult.Error, _) => TestResult.Error
        case (_, TestResult.Error) => TestResult.Error
        case (TestResult.Failed, _) => TestResult.Failed
        case (_, TestResult.Failed) => TestResult.Failed
      }
      Tests.Output(overall,
        testResults.events ++ multiNodeResults.events,
        testResults.summaries ++ multiNodeResults.summaries)
    }.value,
    assemblyMergeStrategy in(MultiJvm, assembly) := {
      case "application.conf" => MergeStrategy.concat
      case "META-INF/aop.xml" => MergeStrategy.concat
      case x =>
        val old = (assemblyMergeStrategy in(MultiJvm, assembly)).value
        old(x)
    },
    Test / fork := true
    // , logLevel := Level.Debug
  )
  .enablePlugins(MultiJvmPlugin)
  .configs(MultiJvm)
  .dependsOn(`contract-interface`, `use-case`, `infrastructure`)

val `api-server` = (project in file("api-server"))
  .enablePlugins(AshScriptPlugin, JavaAgent)
  .settings(baseSettings)
  .settings(dockerCommonSettings)
  .settings(
    name := "thread-weaver-api",
    mainClass in reStart := Some("com.github.j5ik2o.threadWeaver.api.Main"),
    dockerBaseImage := "openjdk:8",
    dockerUsername := Some("j5ik2o"),
    fork in run := true,
    javaAgents += "org.aspectj" % "aspectjweaver" % "1.8.13",
    javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.9" % "runtime;test",
    javaOptions in Universal += "-Dorg.aspectj.tracing.factory=default",
    javaOptions in run ++= Seq(
      s"-Dcom.sun.management.jmxremote.port=${sys.env.getOrElse("JMX_PORT", "8999")}",
      "-Dcom.sun.management.jmxremote.authenticate=false",
      "-Dcom.sun.management.jmxremote.ssl=false",
      "-Dcom.sun.management.jmxremote.local.only=false",
      "-Dcom.sun.management.jmxremote"
    ),
    javaOptions in Universal ++= Seq(
      "-Dcom.sun.management.jmxremote",
      "-Dcom.sun.management.jmxremote.local.only=true",
      "-Dcom.sun.management.jmxremote.authenticate=false"
    ),
    libraryDependencies ++= Seq(
      "com.lightbend.akka.management" %% "akka-management" % akkaManagementVersion,
      "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion,
      "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion,
      "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion,
      "com.github.TanUkkii007" %% "akka-cluster-custom-downing" % "0.0.12",
      "com.github.everpeace" %% "healthchecks-core" % "0.4.0",
      "com.github.everpeace" %% "healthchecks-k8s-probes" % "0.4.0",
      "org.slf4j" % "jul-to-slf4j" % "1.7.26",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.codehaus.janino" % "janino" % "3.0.6"
    )
  ).dependsOn(`interface`)

val gatlingVersion                 = "2.2.3"
val awsSdkVersion = "1.11.169"

lazy val `gatling-test` = (project in file("tools/gatling-test"))
  .enablePlugins(_root_.io.gatling.sbt.GatlingPlugin)
  .settings(gatlingCommonSettings)
  .settings(
    name := "thread-weaver-gatling-test",
    libraryDependencies ++= Seq(
      "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion,
      "io.gatling" % "gatling-test-framework" % gatlingVersion,
      "com.amazonaws" % "aws-java-sdk-core" % awsSdkVersion,
      "com.amazonaws" % "aws-java-sdk-s3" % awsSdkVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion
    ),
    publishArtifact in(GatlingIt, packageBin) := true
  )
  .settings(addArtifact(artifact in(GatlingIt, packageBin), packageBin in GatlingIt))

lazy val `gatling-runner` = (project in file("tools/gatling-runner"))
  .enablePlugins(JavaAppPackaging)
  .settings(gatlingCommonSettings)
  .settings(
    name := "thread-weaver-gatling-runner",
    libraryDependencies ++= Seq(
      "io.gatling" % "gatling-app" % gatlingVersion,
      "com.amazonaws" % "aws-java-sdk-core" % awsSdkVersion,
      "com.amazonaws" % "aws-java-sdk-s3" % awsSdkVersion
    ),
    mainClass in(Compile, bashScriptDefines) := Some("com.github.j5ik2o.gatling.runner.Runner"),
    dockerBaseImage := "openjdk:8",
    dockerUsername := Some("j5ik2o"),
    packageName in Docker := "thread-weaver-gatling-runner",
    dockerUpdateLatest := true,
    dockerCommands ++= Seq(
      Cmd("USER", "root"),
      Cmd("RUN", "mkdir /var/log/gatling"),
      Cmd("RUN", "chown daemon:daemon /var/log/gatling"),
      Cmd("ENV", "TW_GATLING_RESULT_DIR=/var/log/gatling")
    )
  )
  .dependsOn(
    `gatling-test` % "compile->gatling-it"
  )

val root = (project in file("."))
  .settings(baseSettings)
  .settings(
    name := "thread-weaver"
  ).aggregate(`domain`, `use-case`, `interface`, `infrastructure`, `api-server`, `grpc-proto`, `api-client`)
