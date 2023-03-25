package utils

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.math.abs


class FilterNumber(
    private val minValue: Double = -Double.MAX_VALUE,
    private val maxValue: Double = Double.MAX_VALUE,
    private val decimalNumber: Int = -1,
    defaultValue: TextFieldValue = TextFieldValue("")
) : BaseFieldFilter(defaultValue) {

    override fun onFilter(
        inputTextFieldValue: TextFieldValue,
        lastTextFieldValue: TextFieldValue
    ): TextFieldValue {
        return filterInputNumber(inputTextFieldValue, lastTextFieldValue, minValue, maxValue, decimalNumber)
    }

    private fun filterInputNumber(
        inputTextFieldValue: TextFieldValue,
        lastInputTextFieldValue: TextFieldValue,
        minValue: Double = -Double.MAX_VALUE,
        maxValue: Double = Double.MAX_VALUE,
        decimalNumber: Int = -1,
    ): TextFieldValue {
        val inputString = inputTextFieldValue.text
        val lastString = lastInputTextFieldValue.text

        val newString = StringBuffer()
        val supportNegative = minValue < 0
        var dotIndex = -1
        var isNegative = false

        if (supportNegative && inputString.isNotEmpty() && inputString.first() == '-') {
            isNegative = true
            newString.append('-')
        }

        for (c in inputString) {
            when (c) {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    newString.append(c)
                    val tempValue = newString.toString().toDouble()
                    if (tempValue > maxValue) newString.deleteCharAt(newString.lastIndex)
                    if (tempValue < minValue) newString.deleteCharAt(newString.lastIndex) // TODO 需要改进 （例如限制最小值为 100000000，则将无法输入东西）

                    if (dotIndex != -1) {
                        if (decimalNumber != -1) {
                            val decimalCount = (newString.length - dotIndex - 1).coerceAtLeast(0)
                            if (decimalCount > decimalNumber) newString.deleteCharAt(newString.lastIndex)
                        }
                    }
                }
                '.' -> {
                    if (decimalNumber != 0) {
                        if (dotIndex == -1) {
                            if (newString.isEmpty()) {
                                if (abs(minValue) < 1) {
                                    newString.append("0.")
                                    dotIndex = newString.lastIndex
                                }
                            } else {
                                newString.append(c)
                                dotIndex = newString.lastIndex
                            }

                            if (newString.isNotEmpty() && newString.toString().toDouble() == maxValue) {
                                dotIndex = -1
                                newString.deleteCharAt(newString.lastIndex)
                            }
                        }
                    }
                }
            }
        }

        val textRange: TextRange
        if (inputTextFieldValue.selection.collapsed) { // 表示的是光标范围
            if (inputTextFieldValue.selection.end != inputTextFieldValue.text.length) { // 光标没有指向末尾
                var newPosition = inputTextFieldValue.selection.end + (newString.length - inputString.length)
                if (newPosition < 0) {
                    newPosition = inputTextFieldValue.selection.end
                }
                textRange = TextRange(newPosition)
            }
            else { // 光标指向了末尾
                textRange = TextRange(newString.length)
            }
        }
        else {
            textRange = TextRange(newString.length)
        }

        return lastInputTextFieldValue.copy(
            text = newString.toString(),
            selection = textRange
        )
    }
}

class FilterOnlyChar(defaultValue: TextFieldValue = TextFieldValue("")) : BaseFieldFilter(defaultValue) {
    private var allowSet: Set<Char> = emptySet()

    constructor(allowSet: String, defaultValue: TextFieldValue = TextFieldValue("")) : this(defaultValue) {
        val tempSet = mutableSetOf<Char>()
        for (c in allowSet) {
            tempSet.add(c)
        }
        this.allowSet = tempSet
    }

    constructor(allowSet: Set<Char>, defaultValue: TextFieldValue = TextFieldValue("")) : this(defaultValue) {
        this.allowSet = allowSet
    }

    override fun onFilter(
        inputTextFieldValue: TextFieldValue,
        lastTextFieldValue: TextFieldValue
    ): TextFieldValue {
        return filterOnlyChar(
            inputTextFieldValue,
            lastTextFieldValue,
            allowChar = allowSet
        )
    }

    private fun filterOnlyChar(
        inputTextFiled: TextFieldValue,
        lastTextFiled: TextFieldValue,
        allowChar: Set<Char>
    ): TextFieldValue {
        if (allowChar.isEmpty()) return inputTextFiled // 如果允许列表为空则不过滤

        val newString = StringBuilder()

        var modifierEnd = 0

        for (c in inputTextFiled.text) {
            if (c in allowChar) {
                newString.append(c)
            }
            else modifierEnd--
        }

        return inputTextFiled.copy(text = newString.toString())
    }
}

class FilterStandardEmail(
    private val extraChar: String = "",
    defaultValue: TextFieldValue = TextFieldValue("")
) : BaseFieldFilter(defaultValue) {
    private val allowChar: MutableSet<Char> = mutableSetOf('@', '.', '_', '-').apply {
        addAll('0'..'9')
        addAll('a'..'z')
        addAll('A'..'Z')
        addAll(extraChar.asIterable())
    }

    override fun onFilter(
        inputTextFieldValue: TextFieldValue,
        lastTextFieldValue: TextFieldValue
    ): TextFieldValue {
        return inputTextFieldValue.copy(text = filterStandardEmail(inputTextFieldValue.text, lastTextFieldValue.text))
    }

    private fun filterStandardEmail(
        inputString: String,
        lastString: String,
    ): String {
        val newString = StringBuffer()
        var flag = 0 // 0 -> None 1 -> "@" 2 -> "."

        for (c in inputString) {
            if (c !in allowChar) continue

            when (c) {
                '@' -> {
                    if (flag == 0) {
                        if (newString.isNotEmpty() && newString.last() != '.') {
                            if (newString.isNotEmpty()) {
                                newString.append(c)
                                flag++
                            }
                        }
                    }
                }
                '.' -> {
                    // if (flag >= 1) {
                        if (newString.isNotEmpty() && newString.last() != '@' && newString.last() != '.') {
                            newString.append(c)
                            // flag++
                        }
                    // }
                }
                else -> {
                    newString.append(c)
                }
            }
        }

        return newString.toString()
    }

}

class FilterColorHex(
    private val includeAlpha: Boolean = true,
    defaultValue: TextFieldValue = TextFieldValue("")
) : BaseFieldFilter(defaultValue) {

    override fun onFilter(
        inputTextFieldValue: TextFieldValue,
        lastTextFieldValue: TextFieldValue
    ): TextFieldValue {
        return inputTextFieldValue.copy(filterInputColorHex(
            inputTextFieldValue.text,
            lastTextFieldValue.text,
            includeAlpha
        ))
    }

    private fun filterInputColorHex(
        inputValue: String,
        lastValue: String,
        includeAlpha: Boolean = true
    ): String {
        val maxIndex = if (includeAlpha) 8 else 6
        val newString = StringBuffer()
        var index = 0

        for (c in inputValue) {
            if (index > maxIndex) break

            if (index == 0) {
                if (c == '#') {
                    newString.append(c)
                    index++
                }
            }
            else {
                if (c in '0'..'9' || c.uppercase() in "A".."F" ) {
                    newString.append(c.uppercase())
                    index++
                }
            }
        }

        return newString.toString()
    }
}

/**
 * 过滤输入内容长度
 *
 * @param maxLength 允许输入长度，如果 小于 0 则不做过滤，直接返回原数据
 * */
class FilterMaxLength(
    private val maxLength: Int,
    defaultValue: TextFieldValue = TextFieldValue("")
) : BaseFieldFilter(defaultValue) {
    override fun onFilter(
        inputTextFieldValue: TextFieldValue,
        lastTextFieldValue: TextFieldValue
    ): TextFieldValue {
        return filterMaxLength(inputTextFieldValue, lastTextFieldValue, maxLength)
    }

    private fun filterMaxLength(
        inputTextField: TextFieldValue,
        lastTextField: TextFieldValue,
        maxLength: Int
    ): TextFieldValue {
        if (maxLength < 0) return inputTextField // 错误的长度，不处理直接返回

        if (inputTextField.text.length <= maxLength) return inputTextField // 总计输入内容没有超出长度限制


        // 输入内容超出了长度限制
        // 这里要分两种情况：
        // 1. 直接输入的，则返回原数据即可
        // 2. 粘贴后会导致长度超出，此时可能还可以输入部分字符，所以需要判断后截断输入

        val inputCharCount = inputTextField.text.length - lastTextField.text.length
        if (inputCharCount > 1) { // 同时粘贴了多个字符内容
            val allowCount = maxLength - lastTextField.text.length
            // 允许再输入字符已经为空，则直接返回原数据
            if (allowCount <= 0) return lastTextField

            // 还有允许输入的字符，则将其截断后插入
            val newString = StringBuffer()
            newString.append(lastTextField.text)
            val newChar = inputTextField.text.substring(lastTextField.selection.start..allowCount)
            newString.insert(lastTextField.selection.start, newChar)
            return lastTextField.copy(text = newString.toString(), selection = TextRange(lastTextField.selection.start + newChar.length))
        }
        else { // 正常输入
            return if (inputTextField.selection.collapsed) { // 如果当前不是选中状态，则使用上次输入的光标位置，如果使用本次的位置，光标位置会 +1
                lastTextField
            } else { // 如果当前是选中状态，则使用当前的光标位置
                lastTextField.copy(selection = inputTextField.selection)
            }
        }
    }
}


// Only for CHN phone number
class FilterPhone(
    defaultValue: TextFieldValue = TextFieldValue("")
) : BaseFieldFilter(defaultValue) {

    override fun onFilter(
        inputTextFieldValue: TextFieldValue,
        lastTextFieldValue: TextFieldValue
    ): TextFieldValue {
        return filterPhone(inputTextFieldValue, lastTextFieldValue)
    }

    private fun filterPhone(
        inputTextFieldValue: TextFieldValue,
        lastTextFiled: TextFieldValue
    ): TextFieldValue {
        val newString = StringBuilder()

        if (inputTextFieldValue.text.isEmpty()) return inputTextFieldValue

        if (!inputTextFieldValue.selection.collapsed) return inputTextFieldValue

        var pos = inputTextFieldValue.selection.end
        var isFirst = true

        for (c in inputTextFieldValue.text) {
            if (isFirst) {
                if (c == '1') {
                    newString.append('1')
                    isFirst = false
                }
                else {
                    pos--
                }
            }
            else {
                if (c in '0'..'9') {
                    newString.append(c)
                }
                else {
                    pos--
                }
            }

            if (newString.length == 11) break
        }

        return inputTextFieldValue.copy(text = newString.toString(), selection = TextRange(pos.coerceAtLeast(0)))
    }

}

class FilterGMT(
    defaultValue: TextFieldValue = TextFieldValue("")
) : BaseFieldFilter(defaultValue) {

    override fun onFilter(
        inputTextFieldValue: TextFieldValue,
        lastTextFieldValue: TextFieldValue
    ): TextFieldValue {
        return filterInputGMT(inputTextFieldValue, lastTextFieldValue)
    }

    // GMT+08:00
    private fun filterInputGMT(inputTextFieldValue: TextFieldValue, lastTextFiled: TextFieldValue): TextFieldValue {
        val inputValue = inputTextFieldValue.text
        val newValue = StringBuffer()
        val standString = StringBuffer("GMT+08:00")
        var legalIndex = 0
        var pos = inputTextFieldValue.selection.end

        for (c in inputValue) {
            when (legalIndex) {
                0, 1, 2 -> {
                    if (c.uppercase() == standString[legalIndex].uppercase()) {
                        newValue.append(c.uppercase())
                        legalIndex++
                    }
                    else {
                        pos--
                    }
                }
                3 -> {
                    if (c == '+' || c == '-') {
                        newValue.append(c)
                        legalIndex++
                    }
                    else {
                        pos--
                    }
                }
                4 -> {
                    if (c.isDigit()) {
                        newValue.append(c)
                        legalIndex++
                    }
                    else {
                        pos--
                    }
                }
                5 -> {
                    if (c.isDigit()) {
                        newValue.append(c)
                        legalIndex++
                    }
                    if (c == ':') {
                        newValue.insert(4, "0")
                        newValue.append(":")
                        legalIndex++
                        pos++
                    }
                }
                6 -> {
                    if (c == ':') {
                        newValue.append(":")
                        legalIndex++
                    }
                }
                7 -> {
                    if (c.isDigit() && (c.code - '0'.code) <= 5) {
                        newValue.append(c)
                        legalIndex++
                    }
                    else {
                        pos--
                    }
                }
                8 -> {
                    if (c.isDigit()) {
                        newValue.append(c)
                        legalIndex++
                    }
                    else {
                        pos--
                    }
                }
            }
            if (legalIndex > 8) break
        }

        return inputTextFieldValue.copy(text = newValue.toString(), selection = TextRange(pos))
    }
}


open class BaseFieldFilter(defaultValue: TextFieldValue = TextFieldValue("")) {
    private var inputValue = mutableStateOf(defaultValue)

    protected open fun onFilter(inputTextFieldValue: TextFieldValue, lastTextFieldValue: TextFieldValue): TextFieldValue {
        return TextFieldValue()
    }

    protected open fun computePos(): Int {
        // TODO
        return 0
    }

    protected fun getNewTextRange(
        lastTextFiled: TextFieldValue,
        inputTextFieldValue: TextFieldValue
    ): TextRange? {
        if (lastTextFiled.text == inputTextFieldValue.text) return null // 内容没改变，只是光标变动

        return TextRange(lastTextFiled.selection.start, inputTextFieldValue.selection.end)
    }

    protected fun getNewText(
        lastTextFiled: TextFieldValue,
        inputTextFieldValue: TextFieldValue
    ): TextRange? {
        if (lastTextFiled.text == inputTextFieldValue.text) return null // 内容没改变，只是光标变动

        return TextRange(lastTextFiled.selection.start, inputTextFieldValue.selection.end)
    }

    fun getInputValue(): TextFieldValue {
        return inputValue.value
    }

    fun onValueChange(): (TextFieldValue) -> Unit {
        return {
            inputValue.value = onFilter(it, inputValue.value)
        }
    }
}