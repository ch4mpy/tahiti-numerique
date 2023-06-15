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

  function login() {
    console.log('Get login options at: ', bffApiConf.basePath);
    APIs.gateway.getLoginOptions().then(resp => {
      if (resp.data.length > 0) {
        console.log('Login at: ', resp.data[0]);
        Linking.openURL(resp.data[0].loginUri);
      } else {
        console.warn('No login option. Already logged-in?');
      }
    });
  }

  function logout() {
    console.log('logout');
    APIs.gateway.logout();
    setCurrentUser(ANONYMOUS);
  }

  function refresh() {
    console.log('Get user-info');
    return APIs.users
      .getInfo()
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
        console.log('Get greeting');
        APIs.greetings
          .getGreeting()
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
          <Button onPress={refresh} title="Refresh" />
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
