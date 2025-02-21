package id.xtramanagersoftware.xk.manager

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlin.system.exitProcess

@Suppress("DEPRECATION")
class SysDestroyedEasterEgg : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION


        setTheme(R.style.Theme_SysDestroyed)

        val imageView = ImageView(this).apply {
            setImageResource(R.drawable.system_destroyed)
            scaleType = ImageView.ScaleType.FIT_XY
        }

        val layoutParams = FrameLayout.LayoutParams(
            1080,
            2400
        )

        imageView.layoutParams = layoutParams

        setContentView(imageView)


        android.os.Handler().postDelayed({
            finishAffinity()
            exitProcess(0)
        }, 6200)
    }
}




