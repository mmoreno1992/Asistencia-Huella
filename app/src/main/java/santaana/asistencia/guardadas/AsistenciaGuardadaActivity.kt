package santaana.asistencia.guardadas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.core.component.get
import santaana.asistencia.R
import santaana.asistencia.databinding.ActivityAsistenciaGuardadaBinding

class AsistenciaGuardadaActivity : AppCompatActivity() {

    lateinit var binding: ActivityAsistenciaGuardadaBinding
    val viewModel: AsistenciaGuardadaViewModel by viewModels()

    val rvAdapter: RvAsistenciaGuardadaAdapter by lazy {
        RvAsistenciaGuardadaAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAsistenciaGuardadaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpRv()
        viewModel.getAsistenciasEmpleados().observe(this, {
            if (it.isNotEmpty()) {
                rvAdapter.submitList(it)
                binding.emptyView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.VISIBLE
            }
        })
    }

    private fun setUpRv() {
        binding.rvAsistenciaGuardada.apply {
            layoutManager = LinearLayoutManager(this@AsistenciaGuardadaActivity)
            adapter = rvAdapter
        }
    }
}