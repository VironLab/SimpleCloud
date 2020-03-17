package eu.thesimplecloud.module.permission.permission

import com.fasterxml.jackson.annotation.JsonIgnore

data class Permission(val permissionString: String, val timeoutTimestamp: Long, val active: Boolean) {

    @JsonIgnore
    fun isExpired(): Boolean = (System.currentTimeMillis() > timeoutTimestamp) && timeoutTimestamp != -1L

}