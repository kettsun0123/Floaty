package uno.ketts.floaty

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_sample.*

class SampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        button_show_floaty.setOnClickListener {
            Floaty.make(it, R.layout.layout_floaty_big).setDuration(Floaty.LENGTH_INDEFINITE).show()
        }

        button_replace_floaty.setOnClickListener {
            Floaty.make(it, R.layout.layout_floaty_small).setDuration(Floaty.LENGTH_SHORT).setTransition(CustomTransition()).replace()
        }
    }
}
