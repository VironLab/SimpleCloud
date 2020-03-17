package eu.thesimplecloud.module.permission.manager.command

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.command.ICommandSender
import eu.thesimplecloud.api.player.OfflineCloudPlayer
import eu.thesimplecloud.api.property.Property
import eu.thesimplecloud.base.manager.startup.Manager
import eu.thesimplecloud.launcher.console.command.CommandType
import eu.thesimplecloud.launcher.console.command.ICommandHandler
import eu.thesimplecloud.launcher.console.command.annotations.Command
import eu.thesimplecloud.launcher.console.command.annotations.CommandArgument
import eu.thesimplecloud.launcher.console.command.annotations.CommandSubPath
import eu.thesimplecloud.launcher.extension.sendMessage
import eu.thesimplecloud.module.permission.PermissionPool
import eu.thesimplecloud.module.permission.group.PermissionGroup
import eu.thesimplecloud.module.permission.permission.Permission
import eu.thesimplecloud.module.permission.player.IPermissionPlayer
import eu.thesimplecloud.module.permission.player.PermissionPlayer
import eu.thesimplecloud.module.permission.player.PlayerPermissionGroupInfo
import eu.thesimplecloud.module.permission.player.getPermissionPlayer
import java.lang.Exception
import java.util.concurrent.TimeUnit

@Command("perms", CommandType.CONSOLE_AND_INGAME, "simplecloud.module.permission")
class PermissionCommand : ICommandHandler {


    @CommandSubPath("user <user>")
    fun on(commandSender: ICommandSender, @CommandArgument("user") user: String) {
        val permissionPlayer = getPermissionPlayerByName(user)
        if (permissionPlayer == null) {
            commandSender.sendMessage("manager.command.perms.user-not-exist", "&cUser not found.")
            return
        }
        commandSender.sendMessage("User ${permissionPlayer.getName()}:")
        commandSender.sendMessage("Groups: ${permissionPlayer.getAllNotExpiredPermissionGroups().map { it.getName() }}")
        commandSender.sendMessage("Permissions:")
        permissionPlayer.getPermissions().forEach {
            commandSender.sendMessage("- ${it.permissionString} : ${it.active}")
        }
    }

    fun getPermissionPlayerByName(name: String): IPermissionPlayer? {
        val offlinePlayer = CloudAPI.instance.getCloudPlayerManager().getOfflineCloudPlayer(name).awaitUninterruptibly().get()
                ?: return null
        return offlinePlayer.getPermissionPlayer()
    }

    fun updatePermissionPlayer(permissionPlayer: IPermissionPlayer) {
        val offlineCloudPlayerHandler = Manager.instance.offlineCloudPlayerHandler
        val cloudPlayer = permissionPlayer.getCloudPlayer().get()
        if (cloudPlayer != null) {
            permissionPlayer.update()
            offlineCloudPlayerHandler.saveCloudPlayer(cloudPlayer.toOfflinePlayer() as OfflineCloudPlayer)
        } else {
            val offlinePlayer = offlineCloudPlayerHandler.getOfflinePlayer(permissionPlayer.getName()) ?: return
            offlinePlayer.setProperty(PermissionPlayer.PROPERTY_NAME, Property(permissionPlayer))
            offlineCloudPlayerHandler.saveCloudPlayer(offlinePlayer as OfflineCloudPlayer)
        }
    }

    @CommandSubPath("user <user> addGroup <group> <days>")
    fun on(commandSender: ICommandSender, @CommandArgument("user") user: String, @CommandArgument("group") group: String, @CommandArgument("days") days: String) {
        val permissionPlayer = getPermissionPlayerByName(user)
        if (permissionPlayer == null) {
            commandSender.sendMessage("manager.command.perms.user-not-exist", "&cUser not found.")
            return
        }
        val permissionGroup = PermissionPool.instance.getPermissionGroupManager().getPermissionGroupByName(group)
        if (permissionGroup == null) {
            commandSender.sendMessage("manager.command.perms.group-not-exist", "&cGroup not found.")
            return
        }
        val isInt = try {
            days.toInt()
            true
        } catch (ex: Exception) {
            false
        }
        if (days.equals("lifetime", true)) {
            permissionPlayer.addPermissionGroup(PlayerPermissionGroupInfo(group, -1))
            updatePermissionPlayer(permissionPlayer)
            commandSender.sendMessage("manager.command.perms.user.group.added.lifetime", "&aAdded group %GROUP%", permissionGroup.getName(), " lifetime to %PLAYER%", permissionPlayer.getName())
            return
        }
        if (!isInt) {
            commandSender.sendMessage("manager.command.perms.user.day-invalid", "&cThe specified day is invalid.")
            return
        }
        permissionPlayer.addPermissionGroup(PlayerPermissionGroupInfo(group, System.currentTimeMillis() + TimeUnit.DAYS.toMillis(days.toLong())))
        updatePermissionPlayer(permissionPlayer)
        commandSender.sendMessage("manager.command.perms.user.group.added.days", "&aAdded group %GROUP%", permissionGroup.getName(), " for %DAYS%", days, " days to %PLAYER%", permissionPlayer.getName())
    }

    @CommandSubPath("user <user> removeGroup <group>")
    fun on(commandSender: ICommandSender, @CommandArgument("user") user: String, @CommandArgument("group") group: String) {
        val permissionPlayer = getPermissionPlayerByName(user)
        if (permissionPlayer == null) {
            commandSender.sendMessage("manager.command.perms.user-not-exist", "&cUser not found.")
            return
        }
        val permissionGroup = PermissionPool.instance.getPermissionGroupManager().getPermissionGroupByName(group)
        if (permissionGroup == null) {
            commandSender.sendMessage("manager.command.perms.user.group-not-exist", "&cGroup not found.")
            return
        }
        permissionPlayer.removePermissionGroup(permissionGroup.getName())
        updatePermissionPlayer(permissionPlayer)
        commandSender.sendMessage("manager.command.perms.user.group.removed", "&7Group &e%GROUP%", permissionGroup.getName(), " &7removed.")
    }

    @CommandSubPath("user <user> addPermission <permission> <days> <active>")
    fun onPermission(commandSender: ICommandSender, @CommandArgument("user") user: String, @CommandArgument("permission") permission: String, @CommandArgument("days") days: String, @CommandArgument("active") active: String) {
        val permissionPlayer = getPermissionPlayerByName(user)
        if (permissionPlayer == null) {
            commandSender.sendMessage("manager.command.perms.user-not-exist", "&cUser not found.")
            return
        }

        val isInt = try {
            days.toInt()
            true
        } catch (ex: Exception) {
            false
        }
        if (days.equals("lifetime", true)) {
            permissionPlayer.addPermission(Permission(permission, -1, active.toBoolean()))
            updatePermissionPlayer(permissionPlayer)
            commandSender.sendMessage("manager.command.perms.user.permission.added.lifetime", "&aAdded permission %PERMISSION%", permission, " lifetime to %PLAYER%", permissionPlayer.getName())
            return
        }
        if (!isInt) {
            commandSender.sendMessage("manager.command.perms.user.day-invalid", "&cThe specified day is invalid.")
            return
        }
        commandSender.sendMessage("manager.command.perms.user.group.added.days", "&aAdded permission %PERMISSION%", permission, " for %DAYS%", days, " days to %PLAYER%", permissionPlayer.getName())
        permissionPlayer.addPermission(Permission(permission, System.currentTimeMillis() + TimeUnit.DAYS.toMillis(days.toLong()), active.toBoolean()))
        updatePermissionPlayer(permissionPlayer)
    }

    @CommandSubPath("user <user> addPermission <permission> <days>")
    fun onPermission(commandSender: ICommandSender, @CommandArgument("user") user: String, @CommandArgument("permission") permission: String, @CommandArgument("days") days: String) {
        onPermission(commandSender, user, permission, days, true.toString())
    }

    @CommandSubPath("user <user> removePermission <permission>")
    fun onPermission(commandSender: ICommandSender, @CommandArgument("user") user: String, @CommandArgument("permission") permission: String) {
        val permissionPlayer = getPermissionPlayerByName(user)
        if (permissionPlayer == null) {
            commandSender.sendMessage("manager.command.perms.user-not-exist", "&cUser not found.")
            return
        }
        if (!permissionPlayer.hasPermission(permission)) {
            commandSender.sendMessage("manager.command.perms.user.permission.already-removed", "&cThe user doesn't have the specified permission.")
            return
        }
        permissionPlayer.removePermission(permission)
        updatePermissionPlayer(permissionPlayer)
        commandSender.sendMessage("manager.command.perms.user.permission.removed", "&7Permission &e%PERMISSION%", permission, " &7removed.")
    }

    //groups

    fun getPermissionGroupByName(name: String) = PermissionPool.instance.getPermissionGroupManager().getPermissionGroupByName(name)

    @CommandSubPath("groups")
    fun handleGroup(commandSender: ICommandSender) {
        commandSender.sendMessage("&7Permission-Groups:")
        PermissionPool.instance.getPermissionGroupManager().getAllPermissionGroups().forEach {
            commandSender.sendMessage("&8- &e" + it.getName())
        }
    }

    @CommandSubPath("group <group> create")
    fun handleGroupCreate(commandSender: ICommandSender, @CommandArgument("group") group: String) {
        val permissionGroup = getPermissionGroupByName(group)
        if (permissionGroup != null) {
            commandSender.sendMessage("manager.command.perms.group-already-exist", "&cGroup already exist.")
            return
        }
        PermissionGroup(group).update()
        commandSender.sendMessage("manager.command.perms.group.created", "&7Group &e%GROUP%", group, " &7created.")
    }

    @CommandSubPath("group <group>")
    fun handleGroup(commandSender: ICommandSender, @CommandArgument("group") group: String) {
        val permissionGroup = getPermissionGroupByName(group)
        if (permissionGroup == null) {
            commandSender.sendMessage("manager.command.perms.group-not-exist", "&cGroup not found.")
            return
        }
        commandSender.sendMessage("Group: " + permissionGroup.getName())
        permissionGroup.getPermissions().forEach {
            commandSender.sendMessage("- ${it.permissionString} : ${it.active}")
        }
    }

    @CommandSubPath("group <group> addPermission <permission> <active>")
    fun handleGroupPermissionAdd(commandSender: ICommandSender, @CommandArgument("group") group: String, @CommandArgument("permission") permission: String, @CommandArgument("active") active: String) {
        val permissionGroup = getPermissionGroupByName(group)
        if (permissionGroup == null) {
            commandSender.sendMessage("manager.command.perms.group-not-exist", "&cGroup not found.")
            return
        }
        permissionGroup.addPermission(Permission(permission, -1, active.toBoolean()))
        permissionGroup as PermissionGroup
        permissionGroup.update()
        commandSender.sendMessage("manager.command.perms.group.permission-added", "&7Added permission %PERMISSION%", permission, " to group %GROUP%", permissionGroup.getName(), ".")
    }

    @CommandSubPath("group <group> addPermission <permission>")
    fun handleGroupPermissionAdd(commandSender: ICommandSender, @CommandArgument("group") group: String, @CommandArgument("permission") permission: String) {
        handleGroupPermissionAdd(commandSender, group, permission, true.toString())
    }

    @CommandSubPath("group <group> removePermission <permission>")
    fun handleGroupPermissionRemove(commandSender: ICommandSender, @CommandArgument("group") group: String, @CommandArgument("permission") permission: String) {
        val permissionGroup = getPermissionGroupByName(group)
        if (permissionGroup == null) {
            commandSender.sendMessage("manager.command.perms.group-not-exist", "&cGroup not found.")
            return
        }
        if (!permissionGroup.hasPermission(permission)) {
            commandSender.sendMessage("manager.command.perms.group.permission.already-removed", "&cThe group doesn't have the specified permission.")
            return
        }
        permissionGroup.removePermission(permission)
        permissionGroup as PermissionGroup
        permissionGroup.update()
        commandSender.sendMessage("manager.command.perms.group.permission.removed", "&7Permission &e%PERMISSION%", permission, " &7removed.")
    }

    @CommandSubPath("group <group> addInheritance <otherGroup>")
    fun handleInheritanceAdd(commandSender: ICommandSender, @CommandArgument("group") group: String, @CommandArgument("otherGroup") otherGroup: String) {
        val permissionGroup = getPermissionGroupByName(group)
        if (permissionGroup == null) {
            commandSender.sendMessage("manager.command.perms.group-not-exist", "&cGroup not found.")
            return
        }
        val otherPermissionGroup = getPermissionGroupByName(otherGroup)
        if (otherPermissionGroup == null) {
            commandSender.sendMessage("manager.command.perms.other-group.not-exist", "&cOther group not found.")
            return
        }
        if (otherPermissionGroup == permissionGroup) {
            commandSender.sendMessage("manager.command.perms.inheritance.self-error", "&cThe group cannot inherit from itself.")
            return
        }
        if (otherPermissionGroup.getAllInheritedPermissionGroups().contains(permissionGroup)) {
            commandSender.sendMessage("manager.command.perms.inheritance.recursive-error", "&cThe other group is already inheriting the group.")
            return
        }
        if (permissionGroup.addInheritedPermissionGroup(otherPermissionGroup)) {
            commandSender.sendMessage("manager.command.perms.inheritance.add.success", "&7Group &e%GROUP%", permissionGroup.getName(), " &7is now inheriting &e%OTHER_GROUP%", otherPermissionGroup.getName(), "&7.")
        } else {
            commandSender.sendMessage("manager.command.perms.inheritance.add.failure", "&cGroup %GROUP%", permissionGroup.getName(), " is already inheriting %OTHER_GROUP%", otherPermissionGroup.getName(), ".")
        }
        permissionGroup as PermissionGroup
        permissionGroup.update()
    }

    @CommandSubPath("group <group> removeInheritance <otherGroup>")
    fun handleInheritanceRemove(commandSender: ICommandSender, @CommandArgument("group") group: String, @CommandArgument("otherGroup") otherGroup: String) {
        val permissionGroup = getPermissionGroupByName(group)
        if (permissionGroup == null) {
            commandSender.sendMessage("manager.command.perms.group-not-exist", "&cGroup not found.")
            return
        }
        val otherPermissionGroup = getPermissionGroupByName(otherGroup)
        if (otherPermissionGroup == null) {
            commandSender.sendMessage("manager.command.perms.other-group.not-exist", "&cOther group not found.")
            return
        }

        if (permissionGroup.removeInheritedPermissionGroup(otherPermissionGroup)) {
            commandSender.sendMessage("manager.command.perms.inheritance.remove.success", "&7Group &e%GROUP%", permissionGroup.getName(), " &7is no longer inheriting &e%OTHER_GROUP%", otherPermissionGroup.getName(), "&7.")
        } else {
            commandSender.sendMessage("manager.command.perms.inheritance.remove.failure", "&cGroup &e%GROUP%", permissionGroup.getName(), " &7is not inheriting &e%OTHER_GROUP%", otherPermissionGroup.getName(), "&7.")
        }
        permissionGroup as PermissionGroup
        permissionGroup.update()
    }

}