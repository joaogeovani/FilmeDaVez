package com.example.filmedavez.data.remote

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

data class PeriodoLancamentoTmdb(
    val inicio: String,
    val fim: String
) {

    init {
        val dataInicial = criarCalendario(inicio)
        val dataFinal = criarCalendario(fim)

        require(!dataInicial.after(dataFinal)) {
            "A data inicial não pode superar a final."
        }
    }

    // Divide um período em dois intervalos consecutivos
    fun dividirAoMeio():
            Pair<PeriodoLancamentoTmdb, PeriodoLancamentoTmdb>? {

        val dataInicial = criarCalendario(inicio)
        val dataFinal = criarCalendario(fim)

        val milissegundosPorDia = 86_400_000L

        val diferencaDias = (
                dataFinal.timeInMillis -
                        dataInicial.timeInMillis
                ) / milissegundosPorDia

        // Não é possível dividir um único dia
        if (diferencaDias == 0L) {
            return null
        }

        // Encontra o ponto central do intervalo
        val dataMeio = (
                dataInicial.clone() as Calendar
                ).apply {
                add(Calendar.DAY_OF_MONTH, (diferencaDias / 2).toInt())
            }

        // O segundo intervalo começa no dia seguinte
        val proximoDia = (
                dataMeio.clone() as Calendar
                ).apply {
                add(Calendar.DAY_OF_MONTH, 1)
            }

        val primeiroPeriodo = PeriodoLancamentoTmdb(
            inicio = inicio,
            fim = formatarData(dataMeio)
        )

        val segundoPeriodo = PeriodoLancamentoTmdb(
            inicio = formatarData(proximoDia),
            fim = fim
        )

        return primeiroPeriodo to segundoPeriodo
    }

    companion object {

        private val fusoHorario =
            TimeZone.getTimeZone("UTC")

        private fun criarCalendario(data: String): Calendar {

            require(
                Regex("""\d{4}-\d{2}-\d{2}""").matches(data)
            ) {
                "Utilize datas no formato AAAA-MM-DD."
            }

            val partes = data.split("-")

            return GregorianCalendar(fusoHorario).apply {
                isLenient = false
                clear()

                set(Calendar.YEAR, partes[0].toInt())
                set(Calendar.MONTH, partes[1].toInt() - 1)
                set(Calendar.DAY_OF_MONTH, partes[2].toInt())

                // Valida a data informada
                timeInMillis
            }
        }

        private fun formatarData(calendario: Calendar): String {

            return String.format(
                Locale.ROOT,
                "%04d-%02d-%02d",
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH) + 1,
                calendario.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
}
