package com.dapps.myweather.utils

import retrofit2.HttpException
import retrofit2.Response

sealed class MyResponse<out T> {
    object LOADING : MyResponse<Nothing>()
    class SUCCESS<T>(val data: T) : MyResponse<T>()
    class ERROR(val errorCode: Int,val errorMessage: String) : MyResponse<Nothing>()
    class EXCEPTION(val exceptionMessage: String) : MyResponse<Nothing>()
}

suspend fun <T> handleResponse(executeResponse: suspend () -> Response<T>): MyResponse<T> {

    val respone = executeResponse()
    val body = respone.body()

    return try {

        if (respone.isSuccessful && respone != null) {
            MyResponse.SUCCESS(body!!)
        } else {
            MyResponse.ERROR(respone.code(), respone.message())
        }
    } catch (http: HttpException) {
        MyResponse.EXCEPTION(respone.message())
    } catch (e: Exception) {
        MyResponse.EXCEPTION(e.message.toString())
    }

}