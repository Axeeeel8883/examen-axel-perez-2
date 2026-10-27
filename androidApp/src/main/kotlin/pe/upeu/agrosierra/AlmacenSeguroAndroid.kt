package pe.upeu.agrosierra
import android.content.Context
import android.security.keystore.*
import java.security.KeyStore
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import org.json.JSONObject
import org.json.JSONArray
import pe.upeu.agrosierra.data.local.AlmacenSeguro
import pe.upeu.agrosierra.domain.model.Sesion
class AlmacenSeguroAndroid(context:Context):AlmacenSeguro {
 private val preferencias=context.getSharedPreferences("sesion_cifrada",Context.MODE_PRIVATE)
 private val clave=run {
  val ks=KeyStore.getInstance("AndroidKeyStore").apply{load(null)}
  if(!ks.containsAlias("agro_jwt"))KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore").apply{
   init(KeyGenParameterSpec.Builder("agro_jwt",KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());generateKey()
  }
  ks.getKey("agro_jwt",null) as javax.crypto.SecretKey
 }
 override fun guardar(s:Sesion){
  val json=JSONObject().put("token",s.token).put("usuario",s.usuario).put("rol",s.rol).put("privilegios",JSONArray(s.privilegios.toList())).put("expiraEn",s.expiraEn).put("nombre",s.nombre).put("edicionGlobal",s.edicionGlobal)
  val c=Cipher.getInstance("AES/GCM/NoPadding").apply{init(Cipher.ENCRYPT_MODE,clave)}
  val cifrado=c.doFinal(json.toString().toByteArray(Charsets.UTF_8))
  check(preferencias.edit().putString("iv",Base64.encodeToString(c.iv,Base64.NO_WRAP)).putString("datos",Base64.encodeToString(cifrado,Base64.NO_WRAP)).commit())
 }
 override fun leer():Sesion?=try {
  val datos=preferencias.getString("datos",null)
  if(datos==null)null else {
   val iv=Base64.decode(preferencias.getString("iv",null),Base64.NO_WRAP)
   val c=Cipher.getInstance("AES/GCM/NoPadding").apply{init(Cipher.DECRYPT_MODE,clave,GCMParameterSpec(128,iv))}
   val j=JSONObject(String(c.doFinal(Base64.decode(datos,Base64.NO_WRAP)),Charsets.UTF_8))
   val p=j.getJSONArray("privilegios")
   Sesion(j.getString("token"),j.getString("usuario"),j.getString("rol"),(0 until p.length()).map{p.getString(it)}.toSet(),j.getLong("expiraEn"),j.getString("nombre"),j.optBoolean("edicionGlobal"))
  }
 }catch(_:Exception){borrar();null}
 override fun borrar(){check(preferencias.edit().clear().commit())}
}
