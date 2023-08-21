/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, {useEffect, useState} from 'react';
import {
  Button,
  Linking,
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import 'react-native-url-polyfill/auto';
import {APIs, bffApiConf} from './apis';

import {
  authorize as oauth2Login,
  logout as oauth2Logout,
} from 'react-native-app-auth';
import jwt_decode from 'jwt-decode';

// base config
const config = {
  issuer: 'https://dev-ch4mpy.eu.auth0.com/',
  clientId: 'bCtRNTuQo43IQclmqxJ9QpYc6ppWeVDH',
  redirectUrl: 'com.c4-soft.openid-training.front-office.mobile.auth:/callback',
  scopes: ['openid profile email offline_access'],
};

class User {
  constructor(
    readonly email: string,
    readonly name: string,
    readonly roles: string[],
  ) {}

  get isAuthenticated(): boolean {
    return !!this.email;
  }
}
const ANONYMOUS = new User('', '', []);

function App(): JSX.Element {
  const [currentUser, setCurrentUser] = useState(ANONYMOUS);
  const [greeting, setGreeting] = useState('');
  const [accessToken, setAccessToken] = useState('');
  const [idToken, setIdToken] = useState('');
  const [idTokenClaims, setIdTokenClaims] = useState('');

  async function login() {
    // use the client to make the auth request and receive the authState
    try {
      const result = await oauth2Login(config);
      setAccessToken(result.accessToken);
      setIdToken(result.idToken);
      const decoded: any = result.idToken ? jwt_decode(result.idToken) : {};
      setIdTokenClaims(decoded);
      setCurrentUser(
        new User(
          decoded['https://c4-soft.com/user']?.name || '',
          decoded['https://c4-soft.com/user']?.email || '',
          decoded['https://c4-soft.com/authorities'] || [],
        ),
      );
      refresh();
      // result includes accessToken, accessTokenExpirationDate and refreshToken
    } catch (error) {
      console.log(error);
    }
    /*
    console.log('Get login options at: ', bffApiConf.basePath);
    APIs.gateway.getLoginOptions().then(resp => {
      if (resp.data.length > 0) {
        console.log('Login at: ', resp.data[0]);
        Linking.openURL(resp.data[0].loginUri);
      } else {
        console.warn('No login option. Already logged-in?');
      }
    });
    */
  }

  function logout() {
    console.log('logout');
    oauth2Logout(config, {
      idToken: idToken,
      postLogoutRedirectUrl: 'com.c4-soft.openid-training.front-office.mobile',
    });
    setAccessToken('');
    setIdToken('');
    setCurrentUser(ANONYMOUS);
    refresh();
    /*
    APIs.gateway.logout();
    setCurrentUser(ANONYMOUS);
    */
  }

  function refresh() {
    console.log('Get user-info with: ', accessToken);
    return APIs.users
      .getInfo({
        headers: !accessToken
          ? {}
          : {
              Authorization: `Bearer ${accessToken}`,
            },
      })
      .then(userInfoResp => {
        console.log('Set user with: ', userInfoResp.data);
        setCurrentUser(
          new User(
            userInfoResp.data.email,
            userInfoResp.data.name,
            userInfoResp.data.roles,
          ),
        );
      })
      .catch(error => {
        console.log('Failed to get userInfo: ', error);
        setCurrentUser(ANONYMOUS);
      })
      .then(() => {
        console.log('Get greeting with: ', accessToken);
        APIs.greetings
          .getGreeting({
            headers: !accessToken
              ? {}
              : {
                  Authorization: `Bearer ${accessToken}`,
                },
          })
          .then(greetingResp => {
            console.log('Got greeting: ', greetingResp.data);
            setGreeting(greetingResp.data.message);
          })
          .catch(error => {
            console.log('Failed to get greeting: ', error);
            setGreeting('Bug!!!');
          });
      });
  }

  useEffect(() => {
    refresh();
  }, []);

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar />
      <ScrollView contentInsetAdjustmentBehavior="automatic">
        <Text>Formation OpenID</Text>
        <Text>Front-End React Native</Text>
        <View>
          <Text>{greeting}</Text>
          {!currentUser.isAuthenticated ? (
            <Button onPress={login} title="Login" />
          ) : (
            <Button onPress={logout} title="Logout" />
          )}
          <Text>{currentUser.name}</Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});

export default App;
