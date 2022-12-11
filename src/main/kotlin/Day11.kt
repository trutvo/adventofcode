package net.t53k

import java.math.BigInteger

object Day11 {
    interface Operand {
        fun value(context: Long): Long

        companion object {
            fun parse(input: String): Operand {
                return if(input == "old") Old() else Constant(input.toLong())
            }
        }
    }
    class Old: Operand {
        override fun value(context: Long): Long {
            return context
        }
    }
    class Constant(val value: Long): Operand {
        override fun value(context: Long): Long {
            return value
        }
    }

    enum class Operator {
        PLUS {
            override fun execute(left: Long, right: Long): Long {
                return left + right
            }
        },
        MULTIPLY {
            override fun execute(left: Long, right: Long): Long {
                return left * right
            }
        };
        abstract fun execute(left: Long, right: Long): Long

        companion object {
            fun parse(input: String): Operator {
                return if(input == "+") PLUS else MULTIPLY
            }
        }
    }

    class Operation(val left: Operand, val operator: Operator, val right: Operand) {
        fun execute(context: Long): Long {
            return operator.execute(left.value(context), right.value(context))
        }

        companion object {
            private val partsRe     = "([0-9a-z]+) ([\\*\\+]) ([0-9a-z]+)".toRegex()
            fun parse(input: String): Operation {
                partsRe.find(input)?.let { p ->
                    val left = Operand.parse(p.groupValues[1])
                    val right = Operand.parse(p.groupValues[3])
                    return Operation(left, Operator.parse(p.groupValues[2]), right)
                }
                throw IllegalArgumentException("inconsistent operation: $input")
            }
        }
    }

    class KeepAwayGame(val monkeys: List<Monkey>) {
        private val monkeyMap = monkeys.associateBy { it.id }
        fun throwItem(item: Long, to: Int) {
            monkeyMap.get(to)?.receiveItem(item)
        }

        fun round() {
            monkeys.forEach { it.round(this) }
        }
    }
    class Monkey(
        val id: Int,
        inItems: List<Long>,
        private val operation: Operation,
        private val testDivisibleBy: Long,
        private val targetMonkeyIfTestTrue: Int,
        private val targetMonkeyIfTestFalse: Int,
        private val damageLevelDivisor: Long
    ) {
        private val items = inItems.toMutableList()
        private var itemsInspected: BigInteger = BigInteger.ZERO
        fun round(game: KeepAwayGame) {
            items.forEach {currentLevel ->
                val newWorryLevel = operation.execute(currentLevel) / damageLevelDivisor
                val divisible = (newWorryLevel % testDivisibleBy) == 0L
                if(divisible) {
                    game.throwItem(newWorryLevel, targetMonkeyIfTestTrue)
                }
                else {
                    game.throwItem(newWorryLevel, targetMonkeyIfTestFalse)
                }
            }
            itemsInspected = itemsInspected + BigInteger.valueOf(items.count().toLong())
            items.clear()
        }

        fun itemsInspected() = itemsInspected

        fun receiveItem(item: Long) {
            items.add(item)
        }

        companion object {
            private val monkeyStartRe   = "Monkey ([0-9]+):".toRegex()
            private val startingItemsRe = "\\s\\sStarting items: ([0-9\\, ]+)".toRegex()
            private val operationRe     = "\\s\\sOperation: new = ([0-9a-z \\*\\+]+)".toRegex()
            private val testRe          = "\\s\\sTest: divisible by ([0-9]+)".toRegex()
            private val ifTrueRe        = "\\s\\s\\s\\sIf true: throw to monkey ([0-9]+)".toRegex()
            private val ifFalseRe       = "\\s\\s\\s\\sIf false: throw to monkey ([0-9]+)".toRegex()
            private fun create(text: String, damageLevelDivisor: Long): Monkey {
                val lines = text.trim().split("\n")
                var line = 0
                monkeyStartRe.find(lines[line])?.let { start ->
                    line++
                    startingItemsRe.find(lines[line])?.let { items ->
                        line++
                        operationRe.find((lines[line]))?.let { operation ->
                            line++
                            testRe.find(lines[line])?.let {test ->
                                line++
                                ifTrueRe.find(lines[line])?.let { ifTrue ->
                                    line++
                                    ifFalseRe.find(lines[line])?.let { ifFalse ->
                                        return Monkey(
                                            start.groupValues[1].toInt(),
                                            items.groupValues[1]
                                                .split(",")
                                                .map { it.trim() }
                                                .map { it.toLong() },
                                            Operation.parse(operation.groupValues[1]),
                                            test.groupValues[1].toLong(),
                                            ifTrue.groupValues[1].toInt(),
                                            ifFalse.groupValues[1].toInt(),
                                            damageLevelDivisor
                                       )
                                    }
                                    throwError(line, lines)
                                }
                                throwError(line, lines)
                            }
                            throwError(line, lines)
                        }
                        throwError(line, lines)
                    }
                    throwError(line, lines)
                }
                throw IllegalArgumentException("inconsistent dataset: $text")
            }

            private fun throwError(line: Int, lines: List<String>) {
                throw IllegalArgumentException("error parsing line $line: '${lines[line]}'")
            }

            fun parse(input: String, damageLevelDivisor: Long): List<Monkey> {
                return input.split("\n\n").map { create(it, damageLevelDivisor) }
            }
        }
    }
}