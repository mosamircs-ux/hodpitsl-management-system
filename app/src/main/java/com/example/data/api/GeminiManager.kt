package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiManager {
    private const val TAG = "GeminiManager"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Checks if the Gemini API key is configured and not a placeholder.
     */
    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrEmpty() && key != "MY_GEMINI_API_KEY" && key != "placeholder"
    }

    /**
     * Generates a smart medical diagnosis summary or clinic recommendations in Arabic
     * based on symptom descriptions and clinic notes.
     */
    suspend fun generateSmartDiagnosis(symptoms: String, doctorName: String, specialty: String): String = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            Log.w(TAG, "Gemini API Key is not configured. Falling back to local offline smart rule system.")
            return@withContext getOfflineFallbackRecommendation(symptoms, specialty)
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val systemInstruction = """
            أنت مساعد طبي ذكي مبتكر تعمل في نظام إدارة مستشفيات سحابي Smart Hospital SaaS. 
            مهمتك هي قراءة الأعراض والشكوى المدخلة للمريض، والعيادة الموجه إليها، واسم الطبيب، 
            ثم تقديم توصيات استشارية أولية ذكية وملخصة ومنظمة باللغة العربية الفصحى.
            يجب أن تحتوي التوصيات على:
            1. تقييم أولي محتمل للأعراض (مع الإشارة الواضحة بأنه تقييم استرشادي وليس تشخيصاً نهائياً).
            2. الفحوصات أو التحاليل المخبرية المقترحة لهذه الحالة.
            3. نصائح توجيهية وقائية للمريض ريثما يقابل الطبيب المختص.
            كن مختصراً ومنظماً، واجعل المظهر الخارجي للتوصيات أنيقاً باستخدام نقاط واضحة.
        """.trimIndent()

        val prompt = parsePrompt(symptoms, doctorName, specialty)

        try {
            // Build request JSON using standard org.json
            val root = JSONObject()
            
            // Contents array
            val contentsArray = org.json.JSONArray()
            val contentObj = JSONObject()
            val partsArray = org.json.JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            root.put("contents", contentsArray)

            // System instruction
            val sysInstructionObj = JSONObject()
            val sysPartsArray = org.json.JSONArray()
            val sysPartObj = JSONObject()
            sysPartObj.put("text", systemInstruction)
            sysPartsArray.put(sysPartObj)
            sysInstructionObj.put("parts", sysPartsArray)
            root.put("systemInstruction", sysInstructionObj)

            // Generation config with low temperature
            val generationConfig = JSONObject()
            generationConfig.put("temperature", 0.3)
            root.put("generationConfig", generationConfig)

            val requestBody = root.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errCode = response.code
                    val errMsg = response.body?.string() ?: ""
                    Log.e(TAG, "API error: Code $errCode, Msg: $errMsg")
                    return@withContext "عذراً، حدث خطأ في الاتصال بنظام الذكاء الاصطناعي (كود $errCode). التشخيص الاحتياطي: \n${getOfflineFallbackRecommendation(symptoms, specialty)}"
                }

                val bodyStr = response.body?.string() ?: return@withContext "لم يتم تلقي استجابة من النظام طبي الطابع."
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "لم يتم توليد أي نص.")
                        }
                    }
                }
                return@withContext "لم يتم تحليل استجابة الذكاء الاصطناعي بشكل سليم. تشخيص احتياطي: \n${getOfflineFallbackRecommendation(symptoms, specialty)}"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failure generating AI response", e)
            return@withContext "فشل الاتصال بخادم الذكاء الاصطناعي بسبب: ${e.localizedMessage}. تشخيص احتياطي ومقترح: \n${getOfflineFallbackRecommendation(symptoms, specialty)}"
        }
    }

    private fun parsePrompt(symptoms: String, doctorName: String, specialty: String): String {
        return """
            الشكوى الحالية للمريض: "$symptoms"
            العيادة والتخصص الموجه إليه: "$specialty"
            الطبيب المعالج: "$doctorName"
            يرجى صياغة التقييم الذكي الاستشاري الفوري.
        """.trimIndent()
    }

    /**
     * Intelligent local fallback system based on symptoms for completely offline mode/missing API Keys.
     */
    private fun getOfflineFallbackRecommendation(symptoms: String, specialty: String): String {
        val s = symptoms.lowercase()
        return when {
            s.contains("صدر") || s.contains("قلب") || s.contains("تنفس") || s.contains("كتمة") -> """
                📋 تقييم ذكي احتياطي (أمراض القلب/الصداع/الصدر):
                • التقييم المبدئي: أعراض مرتبطة بالمنظومة القلبية التنفسية، تستدعي الرعاية الفورية.
                • الفحوصات المقترحة: تخطيط قلب كهربائي (ECG)، فحص إنزيمات القلب، تصوير صدر بالأشعة السينية.
                • توجيه وقائي: حافظ على الهدوء، تجنب بذل أي مجهود بدني مفاجئ، واحصل على قياس فوري لضغط الدم والنبض.
            """.trimIndent()

            s.contains("بطن") || s.contains("مغص") || s.contains("استفراغ") || s.contains("إسهال") || s.contains("وجع") -> """
                📋 تقييم ذكي احتياطي (باطنية وجهاز هضمي):
                • التقييم المبدئي: اضطرابات محتملة في القناة الهضمية أو تهيج معوي.
                • الفحوصات المقترحة: تحليل بول وبراز شامل، تصوير بالموجات فوق الصوتية للبطن (Ultrasound)، تحليل صورة الدم (CBC).
                • توجيه وقائي: التوقف الفوري عن تناول المأكولات الدسمة والمبهرة، شرب سوائل دافئة بكثرة، والحفاظ على رطوبة الجسم.
            """.trimIndent()

            s.contains("حمى") || s.contains("حرارة") || s.contains("رعشة") || s.contains("حراره") -> """
                📋 تقييم ذكي احتياطي (العيادة العامة / الأطفال):
                • التقييم المبدئي: الإصابة بارتفاع درجة حرارة الجسم قد تبين وجود عدوى بكتيرية أو فيروسية نشطة.
                • الفحوصات المقترحة: فحص تعداد خلايا الدم (CBC)، وفحص بول شامل، ومسحة بلعومية عند الحاجة.
                • توجيه وقائي: استخدام كمادات ماء فاتر للجبين والرقبة، وتناول خافض حرارة آمن (مثل الباراسيتامول) تحت إشراف الصيدلي.
            """.trimIndent()

            s.contains("طفل") || s.contains("كحة") || s.contains("صراخ") -> """
                📋 تقييم ذكي احتياطي (طب الأطفال):
                • التقييم المبدئي: شكوى تنفسية أو معوية شائعة لدى الأطفال.
                • الفحوصات المقترحة: فحص معملي كامل وسماعة الطبيب لاستبعاد الاضطراب الصدري.
                • توجيه وقائي: تجنب تعريض الطفل لتيارات الهواء الباردة أو الروائح النفاذة، مع الإكثار من الرضاعة الطبيعية أو السوائل الدافئة.
            """.trimIndent()

            else -> """
                📋 تقييم ذكي احتياطي (استشارة عامة):
                • التقييم المبدئي: شكوى طبية بحاجة إلى تقييم عيادي شامل من قبل الأخصائي لتحديد مسبباتها.
                • الفحوصات المقترحة: فحص العلامات الحيوية الأساسية (الضغط، الحرارة، النبض)، وتحاليل الدم الروتينية.
                • توجيه وقائي: يرجي كتابة أوقات ظهور الأعراض والتفصيل بالطعام والشراب ريثما تقابل طبيب العيادة المختص.
            """.trimIndent()
        }
    }
}
