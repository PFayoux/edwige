package com.pfayoux.edwige.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class RemindersTest {
    private fun at(month: Int, day: Int, hour: Int, minute: Int = 0, year: Int = 2026) =
        LocalDateTime.of(year, month, day, hour, minute)

    @Test fun `before december the first reminder is december 1st at 9`() =
        assertEquals(at(12, 1, 9), Reminders.nextReminder(at(11, 17, 23)))

    @Test fun `before 9 the reminder is today`() =
        assertEquals(at(12, 5, 9), Reminders.nextReminder(at(12, 5, 7, 30)))

    @Test fun `after 9 the reminder is tomorrow`() =
        assertEquals(at(12, 6, 9), Reminders.nextReminder(at(12, 5, 9, 3)))

    @Test fun `last reminder is december 24th`() {
        assertEquals(at(12, 24, 9), Reminders.nextReminder(at(12, 23, 10)))
        assertNull(Reminders.nextReminder(at(12, 24, 9, 5)))
    }
}
