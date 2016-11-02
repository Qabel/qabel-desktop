package de.qabel.desktop.config

import de.qabel.core.config.Account
import de.qabel.core.event.Event

class AccountSelectedEvent(val account: Account) : Event
