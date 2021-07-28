package polyite

import polyite.ScopInfo
import polyite.export.JSCOPInterface
import java.util.logging.Logger
import polyite.config.ConfigRand
import java.util.logging.Level
import polyite.config.Config
import polyite.config.MinimalConfig.NumGeneratorsLimit
import polyite.config.MinimalConfig.NumGeneratorsLimit
import polyite.schedule.sampling.SamplingStrategy
import polyite.schedule.schedule_tree.ScheduleTreeConstruction
import polyite.schedule.hash.ScheduleHash
import polyite.schedule.Dependence
import polyite.schedule.DomainCoeffInfo
import polyite.schedule.ScheduleSpaceUtils

import scala.util.parsing.json.JSON
import java.util.Base64
import java.nio.charset.StandardCharsets
import polyite.config.Config
import java.util.Properties


object IslScheduleMap2IslScheduleTree {
  protected def createTestConfig() : Option[Config] = {
    val props : Properties = new Properties()
    props.setProperty("numMeasurementThreads", "1")
    props.setProperty("rayCoeffsRange", "3")
    props.setProperty("lineCoeffsRange", "3")
    props.setProperty("maxNumRays", "2")
    props.setProperty("maxNumLines", "2")
    props.setProperty("probabilityToCarryDep", "0.4")
    props.setProperty("maxNumSchedsAtOnce", "1")
    props.setProperty("measurementCommand", "/net/home/brauckmann/poly/polyite/polyite/measure_polybench.bash")
    props.setProperty("measurementWorkingDir", "/net/home/brauckmann/poly/polyite/polyite/")
    props.setProperty("measurementTmpDirBase", "/tmp/")
    props.setProperty("measurementTmpDirNamePrefix", "test")
    props.setProperty("benchmarkName", "test")
    props.setProperty("functionName", "test")
    props.setProperty("scopRegionStart", "test.start")
    props.setProperty("scopRegionEnd", "test.end")
    props.setProperty("irFilesLocation", "/net/home/brauckmann/poly/polyite/polyite/polybench-c-4.1")
    props.setProperty("referenceOutputFile", "/dev/null")
    props.setProperty("numExecutionTimeMeasurements", "5")
    props.setProperty("populationFilePrefix", "test")
    props.setProperty("exportSchedulesToJSCOPFiles", "true")
    props.setProperty("jscopFolderPrefix", "test")
    props.setProperty("csvFilePrefix", "test")
    props.setProperty("measurementTimeout", "1")
    props.setProperty("exportPopulationToCSV", "true")
    props.setProperty("logToFile", "true")
    props.setProperty("logFile", "test.log")
    props.setProperty("evaluationSigIntExitCode", "42")
    props.setProperty("randSchedsTimeout", "200")
    props.setProperty("genSchedsMaxAllowedConseqFailures", "1000")
    props.setProperty("numScheds", "1")
    props.setProperty("numScheduleGenThreads", "4")
    props.setProperty("filterImportedPopulation", "false")
    props.setProperty("importScheds", "false")
    props.setProperty("islComputeout", "38400000")
    props.setProperty("barvinokBinary", "/net/home/brauckmann/poly/polyite/barvinok_binary/count_integer_points")
    props.setProperty("barvinokLibraryPath", "/net/home/brauckmann/poly/polyite/barvinok/install/lib")
    props.setProperty("paramValMappings", "n=10")
    props.setProperty("measureParExecTime", "true")
    props.setProperty("measureSeqExecTime", "false")
    props.setProperty("boundSchedCoeffs", "true")
    props.setProperty("moveVertices", "false")
    props.setProperty("rayPruningThreshold", "NONE")
    props.setProperty("seqPollyOptFlags", "-polly-parallel=false -polly-vectorizer=none -polly-tiling=false -polly-process-unprofitable=true")
    props.setProperty("parPollyOptFlags", "-polly-parallel=true -polly-vectorizer=none -polly-tiling=true -polly-default-tile-size=64 -polly-process-unprofitable=true")
    props.setProperty("simplifySchedules", "false")
    props.setProperty("insertSetNodes", "false")
    props.setProperty("compilationTimeout", "300")
    props.setProperty("benchmarkingSurrenderTimeout", "172800")
    props.setProperty("measureCacheHitRatePar", "false")
    props.setProperty("measureCacheHitRateSeq", "false")
    props.setProperty("seed", "NONE")
    props.setProperty("numactlConf", "NONE")
    props.setProperty("linIndepVectsDoNotFixDims", "false")
    props.setProperty("simplifySchedTrees", "true")
    props.setProperty("splitLoopBodies", "true")
    props.setProperty("numCompilatonDurationMeasurements", "1")
    props.setProperty("validateOutput", "true")
    props.setProperty("tilingPermitInnerSeq", "false")
    props.setProperty("schedTreeSimplRebuildDimScheds", "true")
    props.setProperty("schedTreeSimplRemoveCommonOffset", "true")
    props.setProperty("schedTreeSimplDivideCoeffsByGCD", "true")
    props.setProperty("schedTreeSimplElimSuperfluousSubTrees", "true")
    props.setProperty("schedTreeSimplElimSuperfluousDimNodes", "true")
    props.setProperty("normalizeFeatures", "true")
    props.setProperty("evaluationStrategy", "CPU")
    props.setProperty("samplingStrategy", "CHERNIKOVA")
    props.setProperty("scheduleEquivalenceRelation", "RATIONAL_MATRIX_AND_GENERATORS")
    props.setProperty("expectPrevectorization", "false")
    return Config.parseConfig(props)
  }
  
  
  val myLogger : Logger = Logger.getLogger("")

  def main(args : Array[String]) : Unit = {
    val scopStr = new String(Base64.getDecoder().decode(args(0)))    
    val schedStr = new String(Base64.getDecoder().decode(args(1)))
//    println(scopStr)
//    println(schedStr)

    val ctx : isl.Ctx = isl.Isl.ctx
    val sched : isl.UnionMap = isl.UnionMap.readFromStr(ctx, schedStr)

    
       
    var scop : ScopInfo = null
    var deps : Set[Dependence] = null
    var domInfo : DomainCoeffInfo = null
    var conf : Config = null

    scop = JSCOPInterface.parseJSCOP(scopStr) match {
      case None    => throw new RuntimeException()
      case Some(s) => s
    }
    
    val t : (Set[Dependence], DomainCoeffInfo) = ScheduleSpaceUtils.calcDepsAndDomInfo(scop)
    deps = t._1
    domInfo = t._2

    conf = createTestConfig() match {
      case None    => throw new RuntimeException()
      case Some(c) => c
    }    
    
    val schedTree : isl.Schedule = ScheduleTreeConstruction.islUnionMap2IslScheduleTree(sched, domInfo, scop, deps, conf)
    
    println(schedTree.toString())
  }
}