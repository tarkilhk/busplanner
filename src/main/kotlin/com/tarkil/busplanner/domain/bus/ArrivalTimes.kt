package com.tarkil.busplanner.domain.bus

import com.tarkil.busplanner.infra.bus.CityBusHelper
import org.slf4j.LoggerFactory
import java.lang.Thread.sleep
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class ArrivalTimes(val cityBusHelper: CityBusHelper) {
    private val chosenBusStops = mutableListOf<BusStopConfig>()
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val arrivalTimes = mutableListOf<BusStopTime>()
    var isLoaded = false
    private var lastRefreshTime =
        LocalDateTime.now(ZoneId.of("Asia/Hong_Kong")).format(DateTimeFormatter.ofPattern("HH:mm:ss"))

    private var IAlreadyHadA404ErrorAndImInMyRecursiveLoopToTryToFixIt = false
    private var IAlreadyHadADoubleColonErrorAndImInMyRecursiveLoopToTryToFixIt = false

    fun clearDesiredBusStops() {
        chosenBusStops.clear()
    }

    fun addOneDesiredBusStop(newChosenBusStop: BusStopConfig) {
        chosenBusStops.add(newChosenBusStop)
    }

    fun addSeveralDesiredBusStop(newChosenBusStops: MutableList<BusStopConfig>) {
        chosenBusStops.addAll(newChosenBusStops)
    }

    fun getSortedArrivalTimes(): List<BusStopTime> {
        return this.arrivalTimes.sortedWith(compareBy(BusStopTime::arrivalTime24H))
    }

    fun getResult(): HashMap<String, Any> {
        while (!isLoaded) {
            sleep(3_000)
        }
        //TODO : if several entries are -1, and no successful one, I will not send back the -1 issue; need to pick a first one (highly likely that they would all be the same root cause)
        if (this.arrivalTimes.size > 1) {
            this.arrivalTimes.removeAll { it.isAnError }
        }
        val map = HashMap<String, Any>()
        map["lastRefreshTime"] = this.lastRefreshTime
        map["arrivalTimes"] = this.getSortedArrivalTimes()
        map["isLoaded"] = this.isLoaded
        return map
    }

    fun clearPreviousBusTimesForBusNumber(busNumber: String) {
        arrivalTimes.removeAll { it.isAnError }
        arrivalTimes.removeAll { it.busNumber == busNumber }
    }

    fun clearAll() {
        arrivalTimes.clear()
    }

    fun addOne(busStopTime: BusStopTime) {
        arrivalTimes.add(busStopTime)
    }

    fun addSeveral(busStopTimes: MutableList<BusStopTime>) {
        for (busStopTime in busStopTimes) {
            this.addOne(busStopTime)
        }
    }

    fun refreshDataLoop() {
        if (chosenBusStops.size != 0) {
            for (chosenBusStop in this.chosenBusStops) {
                this.refreshDataFor(chosenBusStop)
            }
            this.setToLoaded()
        }
    }

    fun reinitialiseCookiesAndSetGetURLsForAliveSessions() {
        this.cityBusHelper.loadFirstWebPageAndSaveCookies()
        this.cityBusHelper.loadSetGetURLsFromFB()
    }

    fun refreshDataFor(chosenBusStop: BusStopConfig) {
        val responseMap: MutableMap<String, String> = cityBusHelper.setBusStopDetailsAndGetResponseCode(chosenBusStop)

        if (responseMap["statusCode"].equals("200")) {
            if (responseMap["body"].equals("OK")) {
                logger.info("Successfully set the BusStopDetails for bus ${chosenBusStop.busNumber}")
                this.IAlreadyHadA404ErrorAndImInMyRecursiveLoopToTryToFixIt = false
                val result = cityBusHelper.getNextTimesForPreviouslySetBusStop(chosenBusStop.busNumber)
                this.clearPreviousBusTimesForBusNumber(chosenBusStop.busNumber)
                this.addSeveral(result)
                lastRefreshTime =
                    LocalDateTime.now(ZoneId.of("Asia/Hong_Kong")).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            } else if (responseMap["body"].equals("::")) {
                if (this.IAlreadyHadADoubleColonErrorAndImInMyRecursiveLoopToTryToFixIt) {
                    logger.error("I cannot set relevant bus stop details, something must have changed in the cookies, or in the payload I have to send")
                } else {
                    this.IAlreadyHadADoubleColonErrorAndImInMyRecursiveLoopToTryToFixIt = true
                    logger.warn("I'm expecting to receive 'OK' answer when posting the payload to set relevant bus stop details, but I didn't : I will retry to renew my cookies + URLs")
                    cityBusHelper.loadFirstWebPageAndSaveCookies()
                    cityBusHelper.loadSetGetURLsFromFB()
                }
            } else {
                this.clearPreviousBusTimesForBusNumber(chosenBusStop.busNumber)
                this.addSeveral(mutableListOf(BusStopTime("-1", "Couldn't set BusStop", "")))
                logger.error("Error setting BusStopDetails : 200, but result is not OK : '${responseMap["body"]}'")
            }
        } else if (responseMap["statusCode"].equals("404")) {
            // 404 means page has not been found : SetGet URLs must have been modified by CityBus
            // I need to renew them
            if (IAlreadyHadA404ErrorAndImInMyRecursiveLoopToTryToFixIt) {
                //I don't want to go in infinite recursive loops, something is not right, I will raise an error
                this.clearPreviousBusTimesForBusNumber(chosenBusStop.busNumber)
                this.addSeveral(
                    mutableListOf(
                        BusStopTime(
                            chosenBusStop.busNumber,
                            "Couldn't set BusStop RCRSV",
                            "${responseMap["statusCode"]}"
                        )
                    )
                )
                logger.error("Error setting BusStopDetails in my recursive loop for bus ${chosenBusStop.busNumber}")
                IAlreadyHadA404ErrorAndImInMyRecursiveLoopToTryToFixIt = false
            } else {
                this.reinitialiseCookiesAndSetGetURLsForAliveSessions()
                IAlreadyHadA404ErrorAndImInMyRecursiveLoopToTryToFixIt = true
                this.refreshDataFor(chosenBusStop)
            }
        } else {
            this.clearPreviousBusTimesForBusNumber(chosenBusStop.busNumber)
            this.addSeveral(mutableListOf(BusStopTime("0", "Couldn't set BusStop", "${responseMap["statusCode"]}")))
            logger.error("Unknown error setting BusStopDetails : code ${responseMap["statusCode"]} for ${chosenBusStop.busNumber}")
        }
    }

    fun setToLoaded() {
        isLoaded = true
    }

    fun setToNotLoaded() {
        isLoaded = false
    }


}