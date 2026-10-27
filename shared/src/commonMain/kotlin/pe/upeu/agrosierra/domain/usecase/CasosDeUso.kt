package pe.upeu.agrosierra.domain.usecase
import pe.upeu.agrosierra.domain.repository.AgroRepository
import pe.upeu.agrosierra.domain.model.Labor
class SincronizarUseCase(private val repo:AgroRepository) { suspend operator fun invoke()=repo.sincronizar() }
class GuardarLaborUseCase(private val repo:AgroRepository) { suspend operator fun invoke(l:Labor)=repo.guardar(l) }
class IniciarSesionUseCase(private val repo:AgroRepository) { suspend operator fun invoke(u:String,p:String)=repo.login(u,p) }
class CerrarSesionUseCase(private val repo:AgroRepository) { suspend operator fun invoke()=repo.salir() }
