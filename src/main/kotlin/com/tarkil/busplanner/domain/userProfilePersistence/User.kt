package com.tarkil.busplanner.domain.userProfilePersistence

import com.tarkil.busplanner.domain.bus.BusStopConfig
import org.springframework.transaction.annotation.Transactional
import javax.persistence.*


@Entity
@Table(name = "USERS")
data class User(
    @Column(unique = true)
    val name: String,

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private val userId: Long = -1,

    @ManyToMany(fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.ALL))
//        @ManyToMany(cascade = arrayOf(CascadeType.ALL))
    @JoinTable(
        name = "USERS_DESIRED_BUS_STOPS",
        joinColumns = arrayOf(JoinColumn(name = "USER_ID")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "DESIRED_BUS_STOP_ID"))
    )
    val desiredBusStops: MutableList<DesiredBusStop> = mutableListOf<DesiredBusStop>()
) {
    private constructor() : this("")

    override fun toString(): String {
        return String.format(
            "User id=$userId : $name"
        )
    }

    fun getUserId(): Long {
        return this.userId
    }

    fun getAllConfigBusStops(): MutableList<DesiredBusStop> {
        return this.desiredBusStops
    }

    fun getAllConfigBusStopsForGroup(name: String): MutableList<DesiredBusStop> {
        return this.desiredBusStops.filter { it.shortName == name }.toMutableList()
    }

    fun getAllChosenBusStops(): MutableList<BusStopConfig> {
        val chosenBusStops = mutableListOf<BusStopConfig>()
        for (desiredBustStop in desiredBusStops) {
            chosenBusStops.add(BusStopConfig(desiredBustStop.busNumber, desiredBustStop.info_hkbus))
        }
        return chosenBusStops
    }

    fun getAllChosenBusStopsForGroup(name: String): MutableList<BusStopConfig> {
        val chosenBusStops = mutableListOf<BusStopConfig>()
        var nameToQuery: String = name
        if (nameToQuery == "default") {
            //This happens when user logins, and doesn't specify a default config
            //TODO : load default config from DB instead, after isDefault has been implemented
            nameToQuery = "CastleDown   \uD83C\uDFF0 \uD83D\uDC47"
        }
        val desiredBusStops = this.desiredBusStops.filter { it.shortName == nameToQuery }.toMutableList()
        for (desiredBusStop in desiredBusStops) {
            chosenBusStops.add(BusStopConfig(desiredBusStop.busNumber, desiredBusStop.info_hkbus))
        }
        return chosenBusStops
    }

    @Transactional
    fun attachDesiredBusStop(desiredBusStop: DesiredBusStop) {
        this.desiredBusStops.add(desiredBusStop)
    }
}