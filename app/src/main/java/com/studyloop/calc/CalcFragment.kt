package com.studyloop.calc

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.studyloop.databinding.FragmentCalcBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CalcFragment : Fragment() {

    private var _binding: FragmentCalcBinding? = null
    private val binding get() = _binding!!
    private var rewardedAd: RewardedAd? = null
    private var sciUnlocked = false

    // Two engine instances
    private val stdEngine = StandardCalcEngine()
    private val sciEngine = ScientificCalcEngine()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentCalcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        showModeSelector()
        loadRewardedAd()
        setupStandardCalc()
        setupSciCalc()

        binding.cardStd.setOnClickListener { showStandard() }
        binding.cardSci.setOnClickListener {
            if (sciUnlocked) showScientific() else showAdGate()
        }
        binding.btnBackStd.setOnClickListener { showModeSelector() }
        binding.btnBackSci.setOnClickListener { showModeSelector() }
        binding.btnBackAd.setOnClickListener  { showModeSelector() }
        binding.btnWatchAd.setOnClickListener { watchAd() }
    }

    // ── Navigation ────────────────────────────────────────────────────────
    private fun showModeSelector() {
        binding.layoutModeSelector.visibility = View.VISIBLE
        binding.layoutStandard.visibility     = View.GONE
        binding.layoutScientific.visibility   = View.GONE
        binding.layoutAdGate.visibility       = View.GONE
    }
    private fun showStandard()   { binding.layoutModeSelector.visibility = View.GONE; binding.layoutStandard.visibility = View.VISIBLE }
    private fun showScientific() { binding.layoutModeSelector.visibility = View.GONE; binding.layoutScientific.visibility = View.VISIBLE }
    private fun showAdGate()     { binding.layoutModeSelector.visibility = View.GONE; binding.layoutAdGate.visibility = View.VISIBLE }

    // ── Standard Calculator ───────────────────────────────────────────────
    private fun setupStandardCalc() {
        val grid = binding.gridStd
        val buttons = listOf(
            Triple("AC",  "fn",  { stdEngine.clear();      updateStdDisplay() }),
            Triple("+/−", "fn",  { stdEngine.negate();     updateStdDisplay() }),
            Triple("%",   "fn",  { stdEngine.percent();    updateStdDisplay() }),
            Triple("÷",   "op",  { stdEngine.op("÷");      updateStdDisplay() }),
            Triple("7",   "num", { stdEngine.digit("7");   updateStdDisplay() }),
            Triple("8",   "num", { stdEngine.digit("8");   updateStdDisplay() }),
            Triple("9",   "num", { stdEngine.digit("9");   updateStdDisplay() }),
            Triple("×",   "op",  { stdEngine.op("×");      updateStdDisplay() }),
            Triple("4",   "num", { stdEngine.digit("4");   updateStdDisplay() }),
            Triple("5",   "num", { stdEngine.digit("5");   updateStdDisplay() }),
            Triple("6",   "num", { stdEngine.digit("6");   updateStdDisplay() }),
            Triple("−",   "op",  { stdEngine.op("−");      updateStdDisplay() }),
            Triple("1",   "num", { stdEngine.digit("1");   updateStdDisplay() }),
            Triple("2",   "num", { stdEngine.digit("2");   updateStdDisplay() }),
            Triple("3",   "num", { stdEngine.digit("3");   updateStdDisplay() }),
            Triple("+",   "op",  { stdEngine.op("+");      updateStdDisplay() }),
            Triple("0",   "num", { stdEngine.digit("0");   updateStdDisplay() }),   // span 2
            Triple(".",   "num", { stdEngine.dot();        updateStdDisplay() }),
            Triple("=",   "eq",  { stdEngine.equals();     updateStdDisplay() }),
        )

        buttons.forEachIndexed { i, (label, type, action) ->
            val btn = Button(requireContext()).apply {
                text = label; textSize = 18f; setTextColor(Color.WHITE)
                isAllCaps = false
                backgroundTintList = android.content.res.ColorStateList.valueOf(
                    Color.parseColor(when (type) {
                        "op"  -> "#2A2060"
                        "fn"  -> "#252535"
                        "eq"  -> "#6C63FF"
                        else  -> "#1C1C27"
                    })
                )
                val p = (8 * resources.displayMetrics.density).toInt()
                setPadding(p, p, p, p)
            }
            btn.setOnClickListener { action() }

            val spec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            val specRow = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            val lp = GridLayout.LayoutParams(specRow, spec).apply {
                width = 0; height = (62 * resources.displayMetrics.density).toInt()
                val m = (4 * resources.displayMetrics.density).toInt()
                setMargins(m, m, m, m)
                if (label == "0") columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 2, 1f)
            }
            grid.addView(btn, lp)
        }
    }

    private fun updateStdDisplay() {
        binding.tvStdResult.text = stdEngine.display
        binding.tvStdTape.text   = stdEngine.tape
        val len = stdEngine.display.length
        binding.tvStdResult.textSize = when { len > 12 -> 24f; len > 8 -> 32f; else -> 42f }
    }

    // ── Scientific Calculator ─────────────────────────────────────────────
    private fun setupSciCalc() {
        sciEngine.onStateChanged = { updateSciDisplay() }
        buildSciKeypad()
        buildConstantsStrip()

        binding.btnDegRad.setOnClickListener {
            sciEngine.toggleDeg()
            binding.btnDegRad.text = if (sciEngine.isDeg) "DEG" else "RAD"
        }
        binding.rgSciMode.setOnCheckedChangeListener { _, id ->
            sciEngine.mode = when (id) {
                binding.rbMath.id -> ScientificCalcEngine.CalcMode.MATH
                binding.rbDeci.id -> ScientificCalcEngine.CalcMode.DECI
                else              -> ScientificCalcEngine.CalcMode.NORM
            }
        }
    }

    private fun updateSciDisplay() {
        binding.tvSciExpr.text    = sciEngine.expression
        binding.tvSciAns.text     = sciEngine.answer
        binding.tvIndShift.visibility  = if (sciEngine.isShift) View.VISIBLE else View.INVISIBLE
        binding.tvIndAlpha.visibility  = if (sciEngine.isAlpha) View.VISIBLE else View.INVISIBLE
        binding.tvIndHyp.visibility    = if (sciEngine.isHyp)   View.VISIBLE else View.INVISIBLE
        binding.tvIndMem.text          = if (sciEngine.memory != 0.0) "M:${sciEngine.memory}" else ""
    }

    private fun buildSciKeypad() {
        val kp = binding.llSciKeypad

        data class Btn(val main: String, val shift: String = "", val alpha: String = "",
                       val type: String = "fn", val action: () -> Unit)

        val rows: List<List<Btn>> = listOf(
            // Row 0
            listOf(
                Btn("SHIFT", type="shift") { sciEngine.toggleShift(); updateSciDisplay() },
                Btn("ALPHA", type="alpha") { sciEngine.toggleAlpha(); updateSciDisplay() },
                Btn("◀", type="nav") { toast("Cursor ◀") },
                Btn("▶", type="nav") { toast("Cursor ▶") },
                Btn("MODE", type="nav") { toast("MODE") },
                Btn("2nd", type="fn2") { toast("2nd function") }
            ),
            // Row 1
            listOf(
                Btn("CALC","SOLVE=") { toast("CALC: Enter expression then =") },
                Btn("∫dx","d/dx")    { sciEngine.insertText("∫("); updateSciDisplay() },
                Btn("▲", type="nav") { toast("Scroll ▲") },
                Btn("▼", type="nav") { toast("Scroll ▼") },
                Btn("x⁻¹","x!")     { sciEngine.applyFunction(if (sciEngine.isShift) "fact" else "inv"); updateSciDisplay() },
                Btn("logₓy")        { sciEngine.applyFunction("logxy"); updateSciDisplay() }
            ),
            // Row 2 (slim)
            listOf(
                Btn("x/y")   { sciEngine.insertText("/"); updateSciDisplay() },
                Btn("³√x")   { sciEngine.applyFunction("cbrt"); updateSciDisplay() },
                Btn("mod")   { sciEngine.insertText(" mod "); updateSciDisplay() },
                Btn("x³")    { sciEngine.applyFunction("cube"); updateSciDisplay() },
                Btn("ˣ√y")   { sciEngine.applyFunction("yroot"); updateSciDisplay() },
                Btn("10ˣ")   { sciEngine.applyFunction("pow10"); updateSciDisplay() },
                Btn("eˣ")    { sciEngine.applyFunction("ex"); updateSciDisplay() },
                Btn("t")     { sciEngine.insertText("t"); updateSciDisplay() }
            ),
            // Row 3
            listOf(
                Btn("x/y","∠ a")       { sciEngine.insertText("/"); updateSciDisplay() },
                Btn("√x","FACT b")     { sciEngine.applyFunction(if (sciEngine.isShift) "fact" else "sqrt"); updateSciDisplay() },
                Btn("x²","|x| c")      { sciEngine.applyFunction(if (sciEngine.isShift) "abs" else "sq"); updateSciDisplay() },
                Btn("xʸ")              { sciEngine.applyFunction("pow"); updateSciDisplay() },
                Btn("Log","Sin⁻¹ d")   { sciEngine.applyFunction(if (sciEngine.isShift) "pow10" else "log"); updateSciDisplay() },
                Btn("Ln","Cos⁻¹ e")    { sciEngine.applyFunction(if (sciEngine.isShift) "ex" else "ln"); updateSciDisplay() }
            ),
            // Row 4
            listOf(
                Btn("(−)","STO")    { sciEngine.negate(); updateSciDisplay() },
                Btn("°'\"","CLRv") { toast("DMS / Decimal conversion") },
                Btn("hyp","Cot%", type="hyp") { sciEngine.toggleHyp(); updateSciDisplay() },
                Btn("Sin","Sin⁻¹") { sciEngine.applyFunction("sin"); updateSciDisplay() },
                Btn("Cos","Cos⁻¹") { sciEngine.applyFunction("cos"); updateSciDisplay() },
                Btn("Tan","Tan⁻¹") { sciEngine.applyFunction("tan"); updateSciDisplay() }
            ),
            // Row 5
            listOf(
                Btn("RCL","STO")         { sciEngine.mRecall(); updateSciDisplay() },
                Btn("ENG","CLRv")        { sciEngine.toEngineering(); updateSciDisplay() },
                Btn("(","CONST")         { sciEngine.inputParenthesis("("); updateSciDisplay() },
                Btn(")","CONV SI")       { sciEngine.inputParenthesis(")"); updateSciDisplay() },
                Btn("S⇒D","Limit ∞")    { sciEngine.stoD(); updateSciDisplay() },
                Btn("M+","CLR ALL", type="mem") {
                    if (sciEngine.isShift) { sciEngine.mClear(); toast("Memory cleared") }
                    else { sciEngine.mPlus(); toast("M+ = ${sciEngine.memory}") }
                    updateSciDisplay()
                }
            ),
            // Row 6
            listOf(
                Btn("7","","MATRIX", type="num") { sciEngine.inputDigit("7"); updateSciDisplay() },
                Btn("8","","VECTOR", type="num") { sciEngine.inputDigit("8"); updateSciDisplay() },
                Btn("9","","FUNC",   type="num") { sciEngine.inputDigit("9"); updateSciDisplay() },
                Btn("⌫","","nPr GCD", type="del") { sciEngine.backspace(); updateSciDisplay() },
                Btn("AC","","nCr LCM", type="ac")  { sciEngine.clear(); updateSciDisplay() }
            ),
            // Row 7
            listOf(
                Btn("4","","STAT",  type="num") { sciEngine.inputDigit("4"); updateSciDisplay() },
                Btn("5","","CMPLX", type="num") { sciEngine.inputDigit("5"); updateSciDisplay() },
                Btn("6","","DISTR", type="num") { sciEngine.inputDigit("6"); updateSciDisplay() },
                Btn("×","","Pol Ceil", type="op") { sciEngine.inputOperator("×"); updateSciDisplay() },
                Btn("÷","","Rec Floor", type="op") { sciEngine.inputOperator("÷"); updateSciDisplay() }
            ),
            // Row 8
            listOf(
                Btn("1","","COPY",  type="num") { sciEngine.inputDigit("1"); updateSciDisplay() },
                Btn("2","","PASTE", type="num") { sciEngine.inputDigit("2"); updateSciDisplay() },
                Btn("3","","Ran#",  type="num") { sciEngine.inputDigit("3"); updateSciDisplay() },
                Btn("+","","PreAns", type="op") { sciEngine.inputOperator("+"); updateSciDisplay() },
                Btn("−","","History", type="op") { sciEngine.inputOperator("−"); updateSciDisplay() }
            ),
            // Row 9
            listOf(
                Btn("0","","Ran# RanInt", type="num") { sciEngine.inputDigit("0"); updateSciDisplay() },
                Btn(".","", type="num") { sciEngine.inputDot(); updateSciDisplay() },
                Btn("Exp","","π") { sciEngine.insertText("E"); updateSciDisplay() },
                Btn("Ans","","e") { sciEngine.recallAns(); updateSciDisplay() },
                Btn("=","","History", type="eq") { sciEngine.equals(); updateSciDisplay() }
            )
        )

        rows.forEachIndexed { rowIdx, row ->
            val ll = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                val rowLp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (3 * resources.displayMetrics.density).toInt() }
                layoutParams = rowLp
            }

            val isSlim    = rowIdx == 2
            val isNumeric = rowIdx in 6..9
            val btnH = when { isSlim -> 28; isNumeric -> 46; else -> 36 }

            row.forEach { btnDef ->
                val btn = buildSciButton(btnDef.main, btnDef.shift, btnDef.alpha,
                    btnDef.type, btnH, btnDef.action)
                ll.addView(btn)
            }
            kp.addView(ll)
        }
    }

    private fun buildSciButton(main: String, shift: String, alpha: String,
                                type: String, heightDp: Int, action: () -> Unit): View {
        val ctx = requireContext()
        val dp  = resources.displayMetrics.density

        return FrameLayout(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(0, (heightDp * dp).toInt(), 1f).apply {
                marginEnd = (3 * dp).toInt()
            }
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 5 * dp
                setColor(Color.parseColor(when (type) {
                    "shift" -> "#E8A000"; "alpha" -> "#5544CC"
                    "nav"   -> "#2A2A3E"; "mode"  -> "#2A2A3F"
                    "hyp"   -> "#2A2010"; "mem"   -> "#0D2A1A"
                    "num"   -> "#1A1A24"; "op"    -> "#1A1040"
                    "del"   -> "#3A1A1A"; "ac"    -> "#CC3333"
                    "eq"    -> "#6C63FF"; "fn2"   -> "#1e1e2e"
                    else    -> "#222232"
                }))
            }
            setOnClickListener { action() }

            // Shift label (top)
            if (shift.isNotEmpty()) addView(TextView(ctx).apply {
                text = shift; textSize = 6.5f; setTextColor(Color.parseColor("#E8A000"))
                gravity = android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                setPadding(0, (1 * dp).toInt(), 0, 0)
            })

            // Alpha label (bottom)
            if (alpha.isNotEmpty()) addView(TextView(ctx).apply {
                text = alpha; textSize = 6f; setTextColor(Color.parseColor("#8888EE"))
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                setPadding(0, 0, 0, (1 * dp).toInt())
            })

            // Main label
            addView(TextView(ctx).apply {
                text = main
                textSize = when { main.length > 4 -> 8f; main.length > 2 -> 10f; else -> 12f }
                setTextColor(Color.WHITE); gravity = android.view.Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            })
        }
    }

    private fun buildConstantsStrip() {
        val strip = binding.llConstants
        val constants = listOf(
            "π" to { sciEngine.insertConstant("pi"); updateSciDisplay() },
            "e" to { sciEngine.insertConstant("e"); updateSciDisplay() },
            "|x|" to { sciEngine.applyFunction("abs"); updateSciDisplay() },
            "n!" to { sciEngine.applyFunction("fact"); updateSciDisplay() },
            "nPr" to { doNPR() },
            "nCr" to { doNCR() },
            "GCD" to { doGCD() },
            "LCM" to { doLCM() },
            "Ran#" to { toast("Ran# = ${sciEngine.randomNum().format(3)}") },
            "sin⁻¹" to { sciEngine.applyFunction("asin"); updateSciDisplay() },
            "cos⁻¹" to { sciEngine.applyFunction("acos"); updateSciDisplay() },
            "tan⁻¹" to { sciEngine.applyFunction("atan"); updateSciDisplay() },
            "³√x" to { sciEngine.applyFunction("cbrt"); updateSciDisplay() },
            "x³" to { sciEngine.applyFunction("cube"); updateSciDisplay() },
            "10ˣ" to { sciEngine.applyFunction("pow10"); updateSciDisplay() },
            "eˣ" to { sciEngine.applyFunction("ex"); updateSciDisplay() },
            "∞" to { sciEngine.insertText("∞"); updateSciDisplay() }
        )
        constants.forEach { (label, action) ->
            val btn = Button(requireContext()).apply {
                text = label; textSize = 10f; isAllCaps = false
                setTextColor(Color.parseColor("#F9CA24"))
                backgroundTintList = android.content.res.ColorStateList.valueOf(
                    Color.parseColor("#1AF9CA24"))
                val m = (3 * resources.displayMetrics.density).toInt()
                val h = (32 * resources.displayMetrics.density).toInt()
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, h).apply {
                    marginEnd = m
                }
                setPadding((8 * resources.displayMetrics.density).toInt(), 0,
                    (8 * resources.displayMetrics.density).toInt(), 0)
                setOnClickListener { action() }
            }
            strip.addView(btn)
        }
    }

    // ── nPr / nCr / GCD / LCM prompts ────────────────────────────────────
    private fun doNPR() {
        promptTwoNumbers("nPr — Permutations", "n", "r") { n, r ->
            toast("${n}P${r} = ${sciEngine.nPr(n, r).toLong()}")
        }
    }
    private fun doNCR() {
        promptTwoNumbers("nCr — Combinations", "n", "r") { n, r ->
            toast("${n}C${r} = ${sciEngine.nCr(n, r).toLong()}")
        }
    }
    private fun doGCD() {
        promptTwoNumbers("GCD", "a", "b") { a, b ->
            toast("GCD(${a},${b}) = ${sciEngine.gcd(a.toLong(), b.toLong())}")
        }
    }
    private fun doLCM() {
        promptTwoNumbers("LCM", "a", "b") { a, b ->
            toast("LCM(${a},${b}) = ${sciEngine.lcm(a.toLong(), b.toLong())}")
        }
    }
    private fun promptTwoNumbers(title: String, l1: String, l2: String, cb: (Int, Int) -> Unit) {
        val row = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL; setPadding(48,0,48,0) }
        val et1 = EditText(requireContext()).apply { hint = l1; inputType = android.text.InputType.TYPE_CLASS_NUMBER; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
        val et2 = EditText(requireContext()).apply { hint = l2; inputType = android.text.InputType.TYPE_CLASS_NUMBER; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f); setPadding(16,0,0,0) }
        row.addView(et1); row.addView(et2)
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(title).setView(row)
            .setPositiveButton("Calculate") { _, _ ->
                val a = et1.text.toString().toIntOrNull() ?: return@setPositiveButton
                val b = et2.text.toString().toIntOrNull() ?: return@setPositiveButton
                cb(a, b)
            }
            .setNegativeButton("Cancel", null).show()
    }

    // ── AdMob Rewarded ────────────────────────────────────────────────────
    private fun loadRewardedAd() {
        // Test unit ID — replace with real one before release
        RewardedAd.load(requireContext(),
            "ca-app-pub-3940256099942544/5224354917",
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) { rewardedAd = ad }
                override fun onAdFailedToLoad(e: LoadAdError) { rewardedAd = null }
            })
    }

    private fun watchAd() {
        val ad = rewardedAd
        if (ad != null) {
            ad.show(requireActivity()) { _ ->
                // Reward granted — unlock scientific calc
                sciUnlocked = true
                showScientific()
                toast("🎉 Scientific Calculator unlocked!")
                loadRewardedAd()  // pre-load next ad
            }
        } else {
            // No ad loaded yet — simulate for development
            toast("Loading ad… (simulating unlock for dev)")
            binding.btnWatchAd.isEnabled = false
            binding.root.postDelayed({
                sciUnlocked = true
                showScientific()
                binding.btnWatchAd.isEnabled = true
            }, 2000)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

// ── Simple standard calculator engine ────────────────────────────────────
class StandardCalcEngine {
    var display = "0"; var tape = ""; private var prev: Double? = null
    private var pendingOp: String? = null; private var evaled = false

    fun digit(d: String) {
        if (evaled) { display = d; evaled = false }
        else if (display == "0") display = d
        else if (display.length < 12) display += d
    }
    fun dot() { if (!display.contains('.')) display += '.'; evaled = false }
    fun negate() { display = if (display.startsWith("-")) display.drop(1) else "-$display" }
    fun percent() { display = ((display.toDoubleOrNull() ?: 0.0) / 100).toString() }
    fun op(o: String) {
        if (pendingOp != null && prev != null && !evaled) calculate()
        prev = display.toDoubleOrNull(); pendingOp = o; evaled = false
        tape = "$display $o"
    }
    fun equals() { if (pendingOp != null && prev != null) { calculate(); pendingOp = null } }
    fun clear() { display = "0"; tape = ""; prev = null; pendingOp = null; evaled = false }
    private fun calculate() {
        val a = prev ?: return; val b = display.toDoubleOrNull() ?: return
        val r = when (pendingOp) {
            "+" -> a + b; "−" -> a - b; "×" -> a * b
            "÷" -> if (b == 0.0) Double.NaN else a / b; else -> b
        }
        tape = "$a ${pendingOp} $b ="
        display = if (r.isNaN()) "Error" else if (r == kotlin.math.floor(r) && kotlin.math.abs(r) < 1e12) r.toLong().toString()
        else "%.8f".format(r).trimEnd('0').trimEnd('.')
        prev = r; evaled = true
    }
}
