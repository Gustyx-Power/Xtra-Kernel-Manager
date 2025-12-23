package id.xms.xtrakernelmanager.utils

import java.util.Calendar

/**
 * Enum representing supported holidays
 */
enum class Holiday {
    CHRISTMAS,  // December 25
    NEW_YEAR,   // January 1
    RAMADAN,    // 1 Ramadan (Hijri calendar)
    EID_FITR    // 1 Syawal / Idul Fitri (end of Ramadan)
}

/**
 * Utility object to check for holidays
 */
object HolidayChecker {
    
    /**
     * Get current active holiday, if any
     * @return Holiday enum if today is a holiday, null otherwise
     */
    fun getCurrentHoliday(): Holiday? {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        // Check Islamic holidays first (priority order: Eid > Ramadan)
        if (isEidFitri()) {
            return Holiday.EID_FITR
        }
        
        if (isRamadan()) {
            return Holiday.RAMADAN
        }
        
        return when {
            // Christmas: December 25-26
            month == Calendar.DECEMBER && (day == 25 || day == 26) -> Holiday.CHRISTMAS
            // New Year: January 1
            month == Calendar.JANUARY && day == 1 -> Holiday.NEW_YEAR
            else -> null
        }
    }
    
    /**
     * Check if today is in Ramadan period (1-3 Ramadan for popup)
     */
    private fun isRamadan(): Boolean {
        return try {
            val hijriCalendar = android.icu.util.Calendar.getInstance(
                android.icu.util.ULocale("@calendar=islamic-umalqura")
            )
            val hijriMonth = hijriCalendar.get(android.icu.util.Calendar.MONTH)
            val hijriDay = hijriCalendar.get(android.icu.util.Calendar.DAY_OF_MONTH)
            
            // 1-3 Ramadan (month 8 in 0-indexed)
            hijriMonth == 8 && hijriDay in 1..3
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if today is Eid al-Fitr (1-3 Syawal)
     */
    private fun isEidFitri(): Boolean {
        return try {
            val hijriCalendar = android.icu.util.Calendar.getInstance(
                android.icu.util.ULocale("@calendar=islamic-umalqura")
            )
            val hijriMonth = hijriCalendar.get(android.icu.util.Calendar.MONTH)
            val hijriDay = hijriCalendar.get(android.icu.util.Calendar.DAY_OF_MONTH)
            
            // 1-3 Syawal (month 9 in 0-indexed)
            hijriMonth == 9 && hijriDay in 1..3
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if currently in holiday period for decorations
     */
    fun getCurrentHolidayForDecoration(): Holiday? {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        // Check Islamic holiday decorations (priority: Eid > Ramadan)
        if (isEidFitriDecoration()) {
            return Holiday.EID_FITR
        }
        
        if (isRamadanDecoration()) {
            return Holiday.RAMADAN
        }
        
        return when {
            // Christmas decoration: December 24-26
            month == Calendar.DECEMBER && day in 24..26 -> Holiday.CHRISTMAS
            // New Year decoration: December 31 - January 1
            (month == Calendar.DECEMBER && day == 31) || 
            (month == Calendar.JANUARY && day == 1) -> Holiday.NEW_YEAR
            else -> null
        }
    }
    
    /**
     * Check if we're in the Ramadan decoration period (first 5 days)
     */
    private fun isRamadanDecoration(): Boolean {
        return try {
            val hijriCalendar = android.icu.util.Calendar.getInstance(
                android.icu.util.ULocale("@calendar=islamic-umalqura")
            )
            val hijriMonth = hijriCalendar.get(android.icu.util.Calendar.MONTH)
            val hijriDay = hijriCalendar.get(android.icu.util.Calendar.DAY_OF_MONTH)
            
            // First 5 days of Ramadan for decoration
            hijriMonth == 8 && hijriDay in 1..5
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if we're in the Eid al-Fitr decoration period (7 days)
     */
    private fun isEidFitriDecoration(): Boolean {
        return try {
            val hijriCalendar = android.icu.util.Calendar.getInstance(
                android.icu.util.ULocale("@calendar=islamic-umalqura")
            )
            val hijriMonth = hijriCalendar.get(android.icu.util.Calendar.MONTH)
            val hijriDay = hijriCalendar.get(android.icu.util.Calendar.DAY_OF_MONTH)
            
            // First 7 days of Syawal for Eid celebration decoration
            hijriMonth == 9 && hijriDay in 1..7
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get current year
     */
    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
    
    /**
     * Get current Hijri year
     */
    fun getCurrentHijriYear(): Int {
        return try {
            val hijriCalendar = android.icu.util.Calendar.getInstance(
                android.icu.util.ULocale("@calendar=islamic-umalqura")
            )
            hijriCalendar.get(android.icu.util.Calendar.YEAR)
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Check if we should show the holiday dialog
     * @param holiday The holiday to check
     * @param lastShownYear The year when this holiday was last shown
     * @return true if dialog should be shown
     */
    fun shouldShowHolidayDialog(holiday: Holiday, lastShownYear: Int): Boolean {
        val currentYear = when (holiday) {
            Holiday.RAMADAN, Holiday.EID_FITR -> getCurrentHijriYear()
            else -> getCurrentYear()
        }
        return lastShownYear < currentYear
    }
}
