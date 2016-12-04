import sbt.Keys._
import sbt._

object Build extends sbt.Build {  
  val pico_atomic     = "org.pico"              %%  "pico-atomic"           % "0.2.1"
  val pico_disposal   = "org.pico"              %%  "pico-disposal"         % "1.0.8"
  val pico_event      = "org.pico"              %%  "pico-event"            % "6.0.0"
  val pico_logging    = "org.pico"              %%  "pico-logging"          % "4.0.1"

  val specs2_core     = "org.specs2"            %%  "specs2-core"           % "3.8.6"

  implicit class ProjectOps(self: Project) {
    def standard(theDescription: String) = {
      self
          .settings(scalacOptions in Test ++= Seq("-Yrangepos"))
          .settings(publishTo := Some("Releases" at "s3://dl.john-ky.io/maven/releases"))
          .settings(description := theDescription)
          .settings(isSnapshot := true)
          .settings(resolvers += Resolver.sonatypeRepo("releases"))
          .settings(addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.3" cross CrossVersion.binary))
    }

    def notPublished = self.settings(publish := {}).settings(publishArtifact := false)

    def libs(modules: ModuleID*) = self.settings(libraryDependencies ++= modules)

    def testLibs(modules: ModuleID*) = self.libs(modules.map(_ % "test"): _*)
  }

  lazy val `pico-fake` = Project(id = "pico-fake", base = file("pico-fake"))
      .standard("Fake project").notPublished
      .testLibs(specs2_core)

  lazy val `pico-statsd` = Project(id = "pico-statsd", base = file("pico-statsd"))
      .standard("Statsd library")
      .libs(pico_atomic, pico_disposal, pico_event, pico_logging)
      .testLibs(specs2_core)

  lazy val all = Project(id = "pico-statsd-project", base = file("."))
      .notPublished
      .aggregate(`pico-statsd`, `pico-fake`)
}
