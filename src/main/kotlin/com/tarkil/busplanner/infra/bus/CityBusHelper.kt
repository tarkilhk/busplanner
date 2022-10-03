package com.tarkil.busplanner.infra.bus

import com.tarkil.busplanner.domain.bus.BusStopConfig
import com.tarkil.busplanner.domain.bus.BusStopTime
import khttp.responses.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CityBusHelper {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private var urlOfSetBusStop = ""
    private var urlOfGetBusStopETA = ""
    private var cookies: MutableMap<String, String> = mutableMapOf()
    private var sessionId: String = ""
    private val sysIDManager: SysIDManager = SysIDManager()

    init {
        log.info("Initialising CityBusHelper Service")
        reinitialiseSession()
    }

    private fun reinitialiseSession() {
        log.info("Recreating a fresh session")
        loadFirstWebPageAndSaveCookies()
        loadSetGetURLsFromFB()
    }

    final fun loadFirstWebPageAndSaveCookies() {
        this.sysIDManager.resetSysId()
        var counterForUnmanagedCookieKeys = 0

        val response: Response = khttp.get("https://mobile.bravobus.com.hk/nwp3/?f=1&ds=ETA&l=1", timeout = 60.0)
        if (response.statusCode == 200) {
            log.info("Retrieved cookies succesfully from landing page")
            val myOwnCookies = mutableMapOf("PPFARE" to "1")

            for (myCookie in response.cookies) {
                when (myCookie.key) {
                    "ETWEBID" -> myOwnCookies["ETWEBID"] = myCookie.value.split(";")[0]
                    "PHPSESSID" -> myOwnCookies["PHPSESSID"] = myCookie.value.split(";")[0]
                    "PPFARE" -> myOwnCookies["PPFARE"] = myCookie.value.split(";")[0]
                    "LANG" -> myOwnCookies["LANG"] = myCookie.value.split(";")[0]
                    "QRSTOP" -> {}
                    else -> { // Note the block
                        counterForUnmanagedCookieKeys = counterForUnmanagedCookieKeys + 1
                        myOwnCookies[myCookie.key] = myCookie.value.split(";")[0]
                        this.sessionId = myCookie.key
                    }
                }
            }
            if (counterForUnmanagedCookieKeys > 1) {
                log.error("The cookies have changed in the landing webpage, I have now 1 more field that I don't know about : it needs to be excluded")
            }
            this.cookies = myOwnCookies
        } else {
            log.error("Failed at retrieving cookies from landing page [${response.statusCode}] : ${response.text}")
        }
    }

    final fun loadSetGetURLsFromFB() {
        var inMapClickAction = false
        var inShowETA = false

        var foundSetURL = false
        var foundGetURL = false

        val payload = mapOf("ssid" to this.sessionId)
        val response: Response =
            khttp.get("https://mobile.bravobus.com.hk/nwp3/fb.php", params = payload, cookies = this.cookies)

        if (response.statusCode == 200) {
            log.info("Retrieved fb.php content successfully")
            val message: String = response.text.replace(" ", "")
            val myCleanedLines = message.lines().dropWhile { it.startsWith("//") }
            for (line in myCleanedLines) {
                if (line.contains("functionmapclickaction(evt,location){")) {
                    inMapClickAction = true
                }
                if (inMapClickAction) {
                    if (line.contains("makeRequest('")) {
                        this.urlOfSetBusStop = line.split("?")[0].replace("makeRequest('", "")
                        inMapClickAction = false
                        foundSetURL = true
                    }
                }
            }

            for (line in myCleanedLines) {
                if (line.contains("functionshoweta(){")) {
                    inShowETA = true
                }
                if (inShowETA) {
                    if (line.contains("makeRequestref('")) {
                        this.urlOfGetBusStopETA = line.split("?")[0].replace("makeRequestref('", "")
                        inShowETA = false
                        foundGetURL = true
                    }
                }
            }
        } else {
            log.error("Failed at retrieving SetGet URLs in fb.php [${response.statusCode}] : ${response.text}")
        }
        if (!foundGetURL) {
            log.error("Couldn't find GET URL in fb.php")
        }
        if (!foundSetURL) {
            log.error("Couldn't find SET URL in fb.php")
        }
    }

    fun setBusStopDetailsAndGetResponseCode(chosenBusStop: BusStopConfig): MutableMap<String, String> {
        val answer: MutableMap<String, String> = mutableMapOf()

        val payload = mapOf("ssid" to this.sessionId, "info" to buildInfoString(chosenBusStop))
        log.info("Before I try to set bus time for ${chosenBusStop.busNumber}")
        val response: Response =
            khttp.get("https://mobile.bravobus.com.hk/nwp3/$urlOfSetBusStop", params = payload, cookies = this.cookies)

        answer["statusCode"] = response.statusCode.toString()
        answer["body"] = response.text

        return answer
    }

    fun getNextTimesForPreviouslySetBusStop(busStopNumber: String): MutableList<BusStopTime> {
        val arrivalTimes = mutableListOf<BusStopTime>()

        val responseCheckSuccess: Response
        val payLoadCheckSuccess: MutableMap<String, String> = mutableMapOf()

        // I need to checkCall first
        val responseCheckCall: Response = khttp.get(
            "https://mobile.bravobus.com.hk/nwp3/checkCall.php",
            params = mapOf("type" to "ETA", "ssid" to this.sessionId, "sysid" to this.sysIDManager.getNextValidSysID()),
            cookies = this.cookies
        )
        if (responseCheckCall.statusCode == 200) {
            log.info("Retrieved checkCall content successfully")
            val bodyCheckCall: String = responseCheckCall.text
            val resultingDocumentCheckCall: Document = Jsoup.parse(bodyCheckCall)
            val onloadMakeRequest = resultingDocumentCheckCall.select("[onload^=makeRequest]").attr("onload").toString()

            if (onloadMakeRequest.isNotEmpty()) {
                val (URLCheckSuccess, params) = onloadMakeRequest.split('"')[1].split("?")
                payLoadCheckSuccess.put("ssid", this.sessionId)
                payLoadCheckSuccess.put("sysid", this.sysIDManager.getNextValidSysID())
                params.split("&").forEach {
                    payLoadCheckSuccess.put(it.split("=")[0], it.split("=")[1])
                }

                // Now I need to checkSuccess
                responseCheckSuccess = khttp.get(
                    "https://mobile.bravobus.com.hk/nwp3/" + URLCheckSuccess,
                    params = payLoadCheckSuccess,
                    cookies = this.cookies
                )
                if (responseCheckSuccess.statusCode == 200) {
                    log.info("Retrieved checkSuccess content successfully")
                } else {
                    log.error("Could not Check Success - status [" + responseCheckSuccess.statusCode + "] : " + responseCheckSuccess.text)
                }
            } else {
                log.warn("Couldn't find 'makeRequest' property in the CheckCall call - body = " + responseCheckCall.text)
                // This means that the website is asking for some captcha identification

                //// New session doesn't work
                // I will restart from new session
//                this.reinitialiseSession()
//                log.info("Reinitialised my session, returning empty result, and hoping for best luck on next loop")
//                return mutableListOf<BusStopTime>()

                log.info("Let's try to deal with the Google recaptcha")

                val onloadRecaptchaType =
                    resultingDocumentCheckCall.select("[onload^=recaptcha_type]").attr("onload").toString()
                val onloadRecaptchaKey =
                    resultingDocumentCheckCall.select("[onload^=recaptcha_key]").attr("onload").toString()

                var (recaptcha_typeKey, recaptcha_typeValue) = onloadRecaptchaType.split(";")[0].split('=')
                var (recaptcha_keyKey, recaptcha_keyValue) = onloadRecaptchaKey.split(";")[0].split('=')

                recaptcha_typeValue = recaptcha_typeValue.replace("\"", "")
                recaptcha_keyValue = recaptcha_keyValue.replace("\"", "").replace(" ", "")

                val URLCheckSuccessToCallForRecaptcha = "https://mobile.bravobus.com.hk/nwp3/checkSuccess.php"
                payLoadCheckSuccess.put("ssid", this.sessionId)
                payLoadCheckSuccess.put("sysid", this.sysIDManager.getNextValidSysID())
                payLoadCheckSuccess.put("type", recaptcha_typeValue)
                payLoadCheckSuccess.put("checkValue", recaptcha_keyValue)
                payLoadCheckSuccess.put("success", "Y")

                responseCheckSuccess =
                    khttp.get(URLCheckSuccessToCallForRecaptcha, params = payLoadCheckSuccess, cookies = this.cookies)
                if (responseCheckSuccess.statusCode == 200) {
                    log.info("Sent the ReCaptcha details to checkSuccess successfully")
                } else {
                    log.error("Could not send the ReCaptcha details to checkSuccess - status [" + responseCheckSuccess.statusCode + "] : " + responseCheckSuccess.text)
                }
            }

        } else {
            log.error("Could not Check Call - status [" + responseCheckCall.statusCode + "] : " + responseCheckCall.text)
        }

        // Now I can get the ETA

        val payload = mapOf("l" to "1", "ssid" to this.sessionId, "sysid" to this.sysIDManager.getNextNextValidSysID())

        val response: Response = khttp.get(
            "https://mobile.bravobus.com.hk/nwp3/$urlOfGetBusStopETA",
            params = payload,
            cookies = this.cookies
        )
        if (response.statusCode == 200) {
            log.info("Retrieved GetBusStopETA content successfully")

            val message: String = response.text

            val resultingDocument: Document = Jsoup.parse(message)
            val tableRowsNextbus_listitem: Elements = resultingDocument.select("div#nextbus_listitem > table")
            val tableRowsNextbus_list: Elements = resultingDocument.select("div#nextbus_list > table")

            if (tableRowsNextbus_listitem.isNotEmpty()) {
                for (myRow in tableRowsNextbus_listitem) {
                    val listOfLowestLevelTDs = myRow.select("td:not(:has(td))")
                    if (listOfLowestLevelTDs.isNotEmpty()) {
                        val cellWithTime = listOfLowestLevelTDs.first()
                        // distance is the last cell from the table containing the results,so I just select last <td>
                        val cellWithDistance = listOfLowestLevelTDs.last()
                        if (cellWithTime != null && cellWithDistance != null) {
                            arrivalTimes.add(BusStopTime(busStopNumber, cellWithTime.text(), cellWithDistance.text()))
                        } else {
                            log.error("Impossible situation : my list of TDs is not empty, but first() or last() are null")
                        }
                    } else {
                        log.error("GetBusStopETA is successful, contains a table, but cells don't follow expected format for bus $busStopNumber")
                    }
                }
            } else {
                //No bus found
                log.warn("GetBusStopETA is successful, but no Table Rows for bus $busStopNumber")
                if (tableRowsNextbus_list.isNotEmpty()) {
                    val errorMessage = tableRowsNextbus_list[0].select("td")[0].text()
                    log.error(errorMessage)
                    arrivalTimes.add(BusStopTime("-1", "$busStopNumber - $errorMessage", "-1"))
                } else {
                    log.warn("No reason found on CityBus website")
                }
            }
        } else {
            log.error("Failed at retrieving GetBusStopETA [${response.statusCode}] : ${response.text}")
        }
        log.info("End of getNextTimesForPreviouslySetBusStop : going to return the newly found arrival times now")
        return (arrivalTimes)
    }

    fun buildInfoString(chosenBusStop: BusStopConfig): String {
        return chosenBusStop.info_hkbus
    }
}