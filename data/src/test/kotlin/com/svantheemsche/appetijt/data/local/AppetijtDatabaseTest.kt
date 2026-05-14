package com.svantheemsche.appetijt.data.local

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppetijtDatabaseTest {

    @Test
    fun `getDatabase returns database instance`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = AppetijtDatabase.getDatabase(context)
        assertNotNull(db)
    }

    @Test
    fun `database provides daos`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = AppetijtDatabase.getDatabase(context)
        assertNotNull(db.recipeDao())
        assertNotNull(db.mealEntryDao())
    }
}
