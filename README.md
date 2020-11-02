# AtherysParties
A Party plugin created for the A'therys Horizons server. The purpose of
this plugin is to introduce the basic functionality of a party, such as
creating, joining, leaving and removing a party.

This plugin does not provide any integrations with other plugins. That
should be handled by the plugins themselves.

## Commands

| Aliases                  | Permission                     | Description                                                                                  |
|--------------------------|--------------------------------|----------------------------------------------------------------------------------------------|
| `/party`                 | `atherysparties.party`         | Gets the current status of your party                                                        |
| `/party invite <player>` | `atherysparties.party.invite`  | Invites the player to join your party                                                        |
| `/party leave`           | `atherysparties.party.leave`   | Leaves the party you are currently in                                                        |
| `/party leader <player>` | `atherysparties.party.leader`  | Assigns the leader role to a new player in                                                   |
| `/party kick <player>`   | `atherysparties.party.kick`    | Kicks the player from your party ( have to be the party leader to do this )                  |
| `/party disband`         | `atherysparties.party.disband` | Disbands your party and kicks all players from it ( have to be the party leader to do this ) |
| `/party pvp <toggle>`    | `atherysparties.party.pvp`     | Toggles pvp within your party ( have to be the party leader to do this )                     |
