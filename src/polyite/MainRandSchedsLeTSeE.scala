package polyite

import java.util.logging.Logger
import polyite.schedule.DomainCoeffInfo
import polyite.config.ConfigRand
import polyite.schedule.Schedule
import polyite.schedule.ScheduleUtils
import polyite.schedule.CoeffSpaceLeTSeEStyle
import polyite.config.ConfigRandLeTSeEStyle
import java.util.logging.Level
import polyite.schedule.Dependence
import polyite.config.MinimalConfig.NumGeneratorsLimit
import polyite.schedule.sampling.SamplingStrategy
import polyite.schedule.hash.ScheduleHash

object MainRandSchedsLeTSeE {

  val myLogger : Logger = Logger.getLogger("")

  def main(args : Array[String]) : Unit = {

    def buildRandSchedGen(s : ScopInfo) : ((DomainCoeffInfo, Set[Dependence], Int, Set[Schedule], NumGeneratorsLimit, NumGeneratorsLimit, ConfigRandLeTSeEStyle, SamplingStrategy, Schedule => ScheduleHash) => Set[Schedule]) = {
      return {
        (domInfo : DomainCoeffInfo, deps : Set[Dependence], numScheds : Int, basis : Set[Schedule],
        maxNumRays : NumGeneratorsLimit, maxNumLines : NumGeneratorsLimit, conf : ConfigRandLeTSeEStyle,
        sampler : SamplingStrategy, hashSchedules : Schedule => ScheduleHash) =>
          {
            CoeffSpaceLeTSeEStyle.genRandSchedules(s)(domInfo, deps, numScheds, basis, conf, hashSchedules) match {
              case None => {
                myLogger.warning("The search space is empty.")
                throw new RuntimeException()
              }
              case Some(scheds) => scheds
            }
          }
      }
    }

    try {
      MainUtil.runRandExpl[ConfigRandLeTSeEStyle](args,
        ConfigRandLeTSeEStyle.loadAndValidateConfig, buildRandSchedGen)
    } catch {
      case t : Throwable => {
        val logger : Logger = Logger.getLogger("")
        logger.log(Level.SEVERE, "Unhandled throwable", t)
      }
    }
  }
}