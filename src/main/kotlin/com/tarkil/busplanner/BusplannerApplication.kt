package com.tarkil.busplanner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class BusplannerApplication

fun main(args: Array<String>) {
	runApplication<BusplannerApplication>(*args)
}
