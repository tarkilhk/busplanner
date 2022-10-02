package com.tarkil.busplanner.api

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class homeController {
    @RequestMapping("/")
    fun home(): String {
        return "Nothing to see here \uD83D\uDC40\uD83D\uDC40"
    }
}