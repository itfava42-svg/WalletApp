package com.example.walletapp.loan


object LoanCalculator {


    /*
       محاسبه قسط به روش سود ساده

       مبلغ + سود کل
       تقسیم بر تعداد اقساط
    */

    fun calculateMonthlyPayment(

        amount: Long,

        interestRate: Double,

        installmentCount: Int

    ): Long {


        if(amount <= 0 || installmentCount <= 0){
            return 0
        }


        val totalInterest =
            amount * (interestRate / 100.0)


        val total =
            amount + totalInterest


        return (total / installmentCount)
            .toLong()

    }



    fun calculateTotalPayment(

        amount: Long,

        interestRate: Double

    ): Long {


        val interest =
            amount * (interestRate / 100.0)


        return (amount + interest).toLong()

    }


}
