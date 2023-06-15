/* tslint:disable */
/* eslint-disable */
/**
 * OpenAPI definition
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: v0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


import type { Configuration } from './configuration';
import type { AxiosPromise, AxiosInstance, AxiosRequestConfig } from 'axios';
import globalAxios from 'axios';
// Some imports not used depending on template conditions
// @ts-ignore
import { DUMMY_BASE_URL, assertParamExists, setApiKeyToObject, setBasicAuthToObject, setBearerAuthToObject, setOAuthToObject, setSearchParams, serializeDataIfNeeded, toPathString, createRequestFunction } from './common';
import type { RequestArgs } from './base';
// @ts-ignore
import { BASE_PATH, COLLECTION_FORMATS, BaseAPI, RequiredError } from './base';

/**
 * 
 * @export
 * @interface UserInfoDto
 */
export interface UserInfoDto {
    /**
     * 
     * @type {string}
     * @memberof UserInfoDto
     */
    'name': string;
    /**
     * 
     * @type {string}
     * @memberof UserInfoDto
     */
    'email': string;
    /**
     * 
     * @type {Array<string>}
     * @memberof UserInfoDto
     */
    'roles': Array<string>;
}

/**
 * UsersApi - axios parameter creator
 * @export
 */
export const UsersApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getInfo: async (options: AxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/users/me`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'GET', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;


    
            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * 
         * @param {string} email 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getRoles: async (email: string, options: AxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'email' is not null or undefined
            assertParamExists('getRoles', 'email', email)
            const localVarPath = `/users/{email}/roles`
                .replace(`{${"email"}}`, encodeURIComponent(String(email)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'GET', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;


    
            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * 
         * @param {string} email 
         * @param {Array<string>} requestBody 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        updateRoles: async (email: string, requestBody: Array<string>, options: AxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'email' is not null or undefined
            assertParamExists('updateRoles', 'email', email)
            // verify required parameter 'requestBody' is not null or undefined
            assertParamExists('updateRoles', 'requestBody', requestBody)
            const localVarPath = `/users/{email}/roles`
                .replace(`{${"email"}}`, encodeURIComponent(String(email)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'PUT', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;


    
            localVarHeaderParameter['Content-Type'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(requestBody, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * UsersApi - functional programming interface
 * @export
 */
export const UsersApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = UsersApiAxiosParamCreator(configuration)
    return {
        /**
         * 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async getInfo(options?: AxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<UserInfoDto>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.getInfo(options);
            return createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration);
        },
        /**
         * 
         * @param {string} email 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async getRoles(email: string, options?: AxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Array<string>>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.getRoles(email, options);
            return createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration);
        },
        /**
         * 
         * @param {string} email 
         * @param {Array<string>} requestBody 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async updateRoles(email: string, requestBody: Array<string>, options?: AxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<object>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.updateRoles(email, requestBody, options);
            return createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration);
        },
    }
};

/**
 * UsersApi - factory interface
 * @export
 */
export const UsersApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = UsersApiFp(configuration)
    return {
        /**
         * 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getInfo(options?: any): AxiosPromise<UserInfoDto> {
            return localVarFp.getInfo(options).then((request) => request(axios, basePath));
        },
        /**
         * 
         * @param {string} email 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getRoles(email: string, options?: any): AxiosPromise<Array<string>> {
            return localVarFp.getRoles(email, options).then((request) => request(axios, basePath));
        },
        /**
         * 
         * @param {string} email 
         * @param {Array<string>} requestBody 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        updateRoles(email: string, requestBody: Array<string>, options?: any): AxiosPromise<object> {
            return localVarFp.updateRoles(email, requestBody, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * UsersApi - interface
 * @export
 * @interface UsersApi
 */
export interface UsersApiInterface {
    /**
     * 
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof UsersApiInterface
     */
    getInfo(options?: AxiosRequestConfig): AxiosPromise<UserInfoDto>;

    /**
     * 
     * @param {string} email 
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof UsersApiInterface
     */
    getRoles(email: string, options?: AxiosRequestConfig): AxiosPromise<Array<string>>;

    /**
     * 
     * @param {string} email 
     * @param {Array<string>} requestBody 
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof UsersApiInterface
     */
    updateRoles(email: string, requestBody: Array<string>, options?: AxiosRequestConfig): AxiosPromise<object>;

}

/**
 * UsersApi - object-oriented interface
 * @export
 * @class UsersApi
 * @extends {BaseAPI}
 */
export class UsersApi extends BaseAPI implements UsersApiInterface {
    /**
     * 
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof UsersApi
     */
    public getInfo(options?: AxiosRequestConfig) {
        return UsersApiFp(this.configuration).getInfo(options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * 
     * @param {string} email 
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof UsersApi
     */
    public getRoles(email: string, options?: AxiosRequestConfig) {
        return UsersApiFp(this.configuration).getRoles(email, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * 
     * @param {string} email 
     * @param {Array<string>} requestBody 
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof UsersApi
     */
    public updateRoles(email: string, requestBody: Array<string>, options?: AxiosRequestConfig) {
        return UsersApiFp(this.configuration).updateRoles(email, requestBody, options).then((request) => request(this.axios, this.basePath));
    }
}


