/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, {useState} from 'react';
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
import { APIs } from './apis';

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
    console.log('login');
    APIs.gateway.getLoginOptions().then(resp => {
      if (resp.data.length > 0) {
        console.log('Login at: ', resp.data[0]);
        Linking.openURL(resp.data[0].loginUri);
        setCurrentUser(
          new User('ch4mp@c4-soft.com', 'ch4mpy', ['USER_ROLES_EDITOR']),
        );
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
