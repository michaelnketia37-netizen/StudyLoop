package com.studyloop.calc

import kotlin.math.*

/**
 * Full scientific calculator engine matching the Casio fx-style feature set.
 * Used by CalcFragment — both standard and scientific modes.
 */
class ScientificCalcEngine {

    // ── State ─────────────────────────────────────────────────────────────
    var expression: String = "0"
        private set
    var answer: String = ""
        private set
    var memory: Double = 0.0
        private set
    var isShift: Boolean = false
        private set
    var isAlpha: Boolean = false
        private set
    var isHyp: Boolean = false
        private set
    var isDeg: Boolean = true
        private set
    var mode: CalcMode = CalcMode.NORM

    enum class CalcMode { NORM, MATH, DECI }

    var onStateChanged: (() -> Unit)? = null

    // ── Input ─────────────────────────────────────────────────────────────
    fun inputDigit(d: String) {
        if (answer.isNotEmpty()) { expression = d; answer = "" }
        else if (expression == "0" && d != ".") expression = d
        else if (expression.length < 24) expression += d
        notify()
    }

    fun inputDot() {
        val last = expression.split(Regex("[+\\-×÷^]")).last()
        if (!last.contains('.')) { expression += "."; notify() }
    }

    fun inputOperator(op: String) {
        answer = ""
        expression += op
        notify()
    }

    fun inputParenthesis(p: String) {
        if (expression == "0") expression = p else expression += p
        notify()
    }

    fun backspace() {
        expression = if (expression.length <= 1) "0" else expression.dropLast(1).trimEnd()
        answer = ""
        notify()
    }

    fun clear() {
        expression = "0"; answer = ""
        isShift = false; isAlpha = false; isHyp = false
        notify()
    }

    fun negate() {
        expression = if (expression.startsWith("-")) expression.drop(1) else "-$expression"
        notify()
    }

    fun insertText(t: String) {
        if (expression == "0") expression = t else expression += t
        notify()
    }

    // ── SHIFT / ALPHA / HYP toggles ───────────────────────────────────────
    fun toggleShift() { isShift = !isShift; isAlpha = false; notify() }
    fun toggleAlpha() { isAlpha = !isAlpha; isShift = false; notify() }
    fun toggleHyp()   { isHyp = !isHyp; notify() }
    fun toggleDeg()   { isDeg = !isDeg; notify() }

    // ── Equals ────────────────────────────────────────────────────────────
    fun equals(): String {
        return try {
            val expr = preprocessExpression(expression)
            val result = evalExpression(expr)
            if (result.isNaN() || result.isInfinite()) throw ArithmeticException("Math Error")
            val formatted = formatResult(result)
            answer = formatted
            notify()
            formatted
        } catch (e: Exception) {
            answer = "Math ERROR"
            notify()
            "Math ERROR"
        }
    }

    // ── Scientific Functions ──────────────────────────────────────────────
    fun applyFunction(fn: String): String {
        val raw = answer.ifEmpty { expression }
        val v = raw.toDoubleOrNull() ?: return "Enter a number first"

        val toR: (Double) -> Double = { if (isDeg) Math.toRadians(it) else it }
        val frR: (Double) -> Double = { if (isDeg) Math.toDegrees(it) else it }

        val (result, label) = when (fn) {
            // Trig
            "sin"   -> if (isShift && isHyp) Pair(asinh(v), "asinh($v)")
                       else if (isShift)      Pair(frR(asin(v)), "sin⁻¹($v)")
                       else if (isHyp)        Pair(sinh(toR(v)), "sinh($v)")
                       else                   Pair(sin(toR(v)),  "sin($v)")
            "cos"   -> if (isShift && isHyp) Pair(acosh(v), "acosh($v)")
                       else if (isShift)      Pair(frR(acos(v)), "cos⁻¹($v)")
                       else if (isHyp)        Pair(cosh(toR(v)), "cosh($v)")
                       else                   Pair(cos(toR(v)),  "cos($v)")
            "tan"   -> if (isShift && isHyp) Pair(atanh(v), "atanh($v)")
                       else if (isShift)      Pair(frR(atan(v)), "tan⁻¹($v)")
                       else if (isHyp)        Pair(tanh(toR(v)), "tanh($v)")
                       else                   Pair(tan(toR(v)),  "tan($v)")
            // Inverse trig (explicit)
            "asin"  -> Pair(frR(asin(v)), "sin⁻¹($v)")
            "acos"  -> Pair(frR(acos(v)), "cos⁻¹($v)")
            "atan"  -> Pair(frR(atan(v)), "tan⁻¹($v)")
            // Logarithms
            "log"   -> if (isShift) Pair(10.0.pow(v), "10^$v") else Pair(log10(v), "log($v)")
            "ln"    -> if (isShift) Pair(exp(v), "e^$v")       else Pair(ln(v),    "ln($v)")
            "pow10" -> Pair(10.0.pow(v), "10^($v)")
            "ex"    -> Pair(exp(v), "e^($v)")
            // Powers & roots
            "sq"    -> Pair(v * v, "$v²")
            "cube"  -> Pair(v * v * v, "$v³")
            "sqrt"  -> Pair(sqrt(v), "√($v)")
            "cbrt"  -> Pair(cbrt(v), "∛($v)")
            "inv"   -> if (isShift) Pair(factorial(v), "$v!") else Pair(1.0 / v, "1/$v")
            "pow"   -> { insertText("^"); return "Enter exponent then =" }
            "yroot" -> { insertText("root"); return "Enter root then =" }
            "logxy" -> { insertText("logxy"); return "Enter base then =" }
            // Other
            "abs"   -> Pair(abs(v), "|$v|")
            "fact"  -> Pair(factorial(v), "$v!")
            "pct"   -> Pair(v / 100.0, "$v%")
            else    -> return "Unknown function"
        }

        if (result.isNaN() || result.isInfinite()) return "Math Error"
        val r = formatResult(result)
        expression = label
        answer = r
        isShift = false; isHyp = false
        notify()
        return "$label = $r"
    }

    // ── Constants ─────────────────────────────────────────────────────────
    fun insertConstant(c: String) {
        val v = when (c) {
            "pi" -> PI.toBigDecimal().toPlainString()
            "e"  -> E.toBigDecimal().toPlainString()
            else -> return
        }
        if (expression == "0") expression = v else expression += v
        notify()
    }

    // ── Memory ────────────────────────────────────────────────────────────
    fun mPlus() {
        memory += (answer.ifEmpty { expression }).toDoubleOrNull() ?: 0.0
        notify()
    }
    fun mRecall() { insertText(formatResult(memory)) }
    fun mClear()  { memory = 0.0; notify() }

    // ── Engineering notation ──────────────────────────────────────────────
    fun toEngineering(): String {
        val v = (answer.ifEmpty { expression }).toDoubleOrNull() ?: return ""
        return v.toBigDecimal().toEngineeringString().also {
            answer = it; notify()
        }
    }

    // ── nPr, nCr, GCD, LCM ───────────────────────────────────────────────
    fun nPr(n: Int, r: Int): Double = factorial(n.toDouble()) / factorial((n - r).toDouble())
    fun nCr(n: Int, r: Int): Double = factorial(n.toDouble()) / (factorial(r.toDouble()) * factorial((n - r).toDouble()))

    fun gcd(a: Long, b: Long): Long {
        var x = abs(a); var y = abs(b)
        while (y != 0L) { val t = y; y = x % y; x = t }
        return x
    }
    fun lcm(a: Long, b: Long): Long = abs(a * b) / gcd(a, b)
    fun randomNum(): Double = Math.random()

    // ── Ans ───────────────────────────────────────────────────────────────
    fun recallAns() { if (answer.isNotEmpty()) insertText(answer) }

    // ── Fraction / S⇒D ───────────────────────────────────────────────────
    fun stoD(): String {
        val v = (answer.ifEmpty { expression }).toDoubleOrNull() ?: return ""
        // Simple: try to express as fraction
        val frac = toFraction(v)
        answer = frac
        notify()
        return frac
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private fun factorial(n: Double): Double {
        val ni = n.roundToInt()
        if (ni < 0 || ni > 170) throw ArithmeticException("Factorial overflow")
        return (1..ni).fold(1.0) { acc, i -> acc * i }
    }

    private fun Double.roundToInt(): Int = kotlin.math.round(this).toInt()

    private fun formatResult(v: Double): String {
        if (v == kotlin.math.floor(v) && abs(v) < 1e15) {
            return v.toLong().toString()
        }
        var s = "%.10f".format(v).trimEnd('0').trimEnd('.')
        if (s.length > 14) s = "%.8e".format(v)
        return s
    }

    private fun toFraction(v: Double): String {
        if (v == kotlin.math.floor(v)) return v.toLong().toString()
        val tolerance = 1.0e-9
        var h1 = 1L; var h2 = 0L; var k1 = 0L; var k2 = 1L
        var b = v
        do {
            val a = b.toLong()
            val aux = h1; h1 = a * h1 + h2; h2 = aux
            val aux2 = k1; k1 = a * k1 + k2; k2 = aux2
            b = 1.0 / (b - a)
        } while (abs(v - h1.toDouble() / k1) > v * tolerance && k1 < 1000000)
        return "$h1/$k1"
    }

    private fun preprocessExpression(expr: String): String =
        expr.replace("×", "*").replace("÷", "/").replace("−", "-")
            .replace("π", PI.toString()).replace("Ans", answer.ifEmpty { "0" })
            .replace("^", ".pow(")  // will be handled in eval

    private fun evalExpression(expr: String): Double {
        // Safe expression evaluator (no exec/reflection)
        return ExpressionParser(expr).parse()
    }

    private fun notify() { onStateChanged?.invoke() }
}

// ── Simple recursive descent expression parser ────────────────────────────
private class ExpressionParser(private val input: String) {
    private var pos = 0

    fun parse(): Double {
        val result = parseAddSub()
        if (pos < input.length) throw IllegalArgumentException("Unexpected: ${input[pos]}")
        return result
    }

    private fun parseAddSub(): Double {
        var result = parseMulDiv()
        while (pos < input.length) {
            result = when {
                consume('+') -> result + parseMulDiv()
                consume('-') -> result - parseMulDiv()
                else -> break
            }
        }
        return result
    }

    private fun parseMulDiv(): Double {
        var result = parsePow()
        while (pos < input.length) {
            result = when {
                consume('*') -> result * parsePow()
                consume('/') -> { val d = parsePow(); if (d == 0.0) Double.NaN else result / d }
                consume('%') -> result % parsePow()
                else -> break
            }
        }
        return result
    }

    private fun parsePow(): Double {
        val base = parseUnary()
        return if (consume('^')) base.pow(parsePow()) else base
    }

    private fun parseUnary(): Double {
        if (consume('-')) return -parseUnary()
        if (consume('+')) return parseUnary()
        return parseAtom()
    }

    private fun parseAtom(): Double {
        if (consume('(')) {
            val v = parseAddSub()
            consume(')')
            return v
        }
        // Number
        val start = pos
        if (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) {
            while (pos < input.length && (input[pos].isDigit() || input[pos] == '.' || input[pos] == 'E' || input[pos] == 'e' ||
                    (input[pos] == '-' && pos > 0 && (input[pos-1] == 'E' || input[pos-1] == 'e')))) pos++
            return input.substring(start, pos).toDouble()
        }
        throw IllegalArgumentException("Unexpected char at $pos: ${input.getOrNull(pos)}")
    }

    private fun consume(c: Char): Boolean {
        while (pos < input.length && input[pos] == ' ') pos++
        if (pos < input.length && input[pos] == c) { pos++; return true }
        return false
    }
}
