package com.example.barcodeapp.domain

data class ParsedBarcode(
    val raw: String,
    val op: Char,           
    val device1: String?,    
    val device2: String?     
)

// Преобразует строку ШК в структуру. Ожидаем ровно 13 символов, первый — '2'.
// op определяет тип операции: '1' — инициализация пары устройств, '2' — проверка device1, '3' — проверка device2
fun parseBarcode(raw: String): ParsedBarcode? {
    if (raw.length != 13 || raw[0] != '2') return null

    val op = raw[1]
    if (op !in listOf('1', '2', '3')) return null

    // Полезные данные между второй и двенадцатой позициями
    val payload = raw.substring(2, 12) 

    val device1 = when (op) {
        // Для '1' и '2' первые 3 символа — код device1
        '1', '2' -> payload.substring(0, 3) 
        else -> null
    }

    val device2 = when (op) {
        // Для '1' следующие 7 символов — device2; для '3' первые 7 символов — device2
        '1' -> payload.substring(3, 10)    
        '3' -> payload.substring(0, 7)       
        else -> null
    }

    return ParsedBarcode(raw, op, device1, device2)
}
