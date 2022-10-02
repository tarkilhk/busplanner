package com.tarkil.busplanner.domain.userProfilePersistence

import javax.persistence.*


@Entity
@Table(name = "DESIRED_BUS_STOPS")
data class DesiredBusStop(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val desiredBusStopId: Long = -1,

    val shortName: String,

    val busNumber: String,

    val info_hkbus: String,

    //TODO add default config to load when user connects
//        val isDefault : Boolean,

    @ManyToMany(mappedBy = "desiredBusStops")
    val users: List<User> = mutableListOf<User>()
) {
    private constructor() : this(-1, "", "0", "")

    override fun toString(): String {
        return String.format(
            "DesiredBusStop id=$desiredBusStopId : $busNumber - $info_hkbus"
        )
    }
}