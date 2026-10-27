package pe.upeu.agrosierra.domain.usecase
import pe.upeu.agrosierra.domain.model.*
import kotlinx.datetime.*
fun puedeEditarLabor(s:Sesion,l:Labor,ahora:Instant=Clock.System.now()):Boolean {
 if("LABOR_EDITAR" !in s.privilegios)return false
 if(s.edicionGlobal)return true
 if(l.responsable!=s.usuario)return false
 if(l.id<0)return true
 return try {ahora.toEpochMilliseconds()-Instant.parse(l.registradoEn).toEpochMilliseconds() in 0 until 86400000}
 catch(_:IllegalArgumentException){false}
}
