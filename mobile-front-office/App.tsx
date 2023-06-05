/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React from 'react';
import {
  Button,
  Linking,
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
} from 'react-native';

import {Colors} from 'react-native/Libraries/NewAppScreen';
import {Configuration as BffConfiguration, BFFApi} from './c4-soft/bff-api';
import {
  Configuration as UsersConfiguration,
  UsersApi,
} from './c4-soft/users-api';

const bffBasePath = 'https://openid-training.c4-soft.com';

const bffApiConf = new BffConfiguration({
  basePath: bffBasePath,
});
const usersApiConf = new UsersConfiguration({
  basePath: bffBasePath,
});

const bffApi = new BFFApi(bffApiConf);

const usersApi = new UsersApi(usersApiConf);

function App(): JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  var username = '';

  usersApi.getInfo().then(user => {
    console.log(user.data);
    username = user.data.name || '';
  });

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={backgroundStyle}>
        <Text
          style={[
            styles.title,
            {
              color: isDarkMode ? Colors.white : Colors.black,
            },
          ]}>
          Security with BFF
        </Text>
        <Text
          style={[
            styles.username,
            {
              color: isDarkMode ? Colors.white : Colors.black,
            },
          ]}>
          {username}
        </Text>
        <View
          style={{
            backgroundColor: isDarkMode ? Colors.black : Colors.white,
          }}>
          <Button
            onPress={() => {
              bffApi.getLoginOptions().then(async options => {
                const loginUri = options.data[0].loginUri;
                console.log(loginUri);
                await Linking.openURL(
                  updateURLParameter(
                    loginUri,
                    'redirect_uri',
                    'mobile://openid-training.c4-soft.com/react-native',
                  ),
                );
              });
            }}
            title="Login"
          />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  title: {
    fontSize: 24,
    fontWeight: '600',
  },
  username: {
    fontSize: 18,
    fontWeight: '400',
  },
});

export default App;

function updateURLParameter(url: string, param: string, paramVal: string) {
  var newAdditionalURL = '';
  var tempArray = url.split('?');
  var baseURL = tempArray[0];
  var additionalURL = tempArray[1];
  var temp = '';
  if (additionalURL) {
    tempArray = additionalURL.split('&');
    for (var i = 0; i < tempArray.length; i++) {
      if (tempArray[i].split('=')[0] !== param) {
        newAdditionalURL += temp + tempArray[i];
        temp = '&';
      }
    }
  }

  var rows_txt = temp + '' + param + '=' + paramVal;
  return baseURL + '?' + newAdditionalURL + rows_txt;
}
