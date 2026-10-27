package pe.upeu.agrosierra.di
import org.koin.dsl.module
import pe.upeu.agrosierra.data.local.*
import pe.upeu.agrosierra.data.remote.*
import pe.upeu.agrosierra.data.repository.*
import pe.upeu.agrosierra.domain.repository.*
import pe.upeu.agrosierra.domain.usecase.*
import pe.upeu.agrosierra.presentation.AgroViewModel
val redModule=module { single{AgroApi(get())} }
val dataModule=module { single<Reloj>{RelojSistema()};single<AgroRepository>{AgroRepositorioOffline(get(),get(),get(),get())} }
val domainModule=module { factory{SincronizarUseCase(get())};factory{GuardarLaborUseCase(get())};factory{IniciarSesionUseCase(get())};factory{CerrarSesionUseCase(get())} }
val presentationModule=module { factory{AgroViewModel(get(),get(),get(),get(),get())} }
