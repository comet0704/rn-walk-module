/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */
import { createStackNavigator } from '@react-navigation/stack'
import * as Sentry from '@sentry/react-native'
import moment from 'moment'
import React, { useEffect } from 'react'
import { Text } from 'react-native'
import codePush from 'react-native-code-push'
/* global __DEV__ */

const isHermes = () => !!global.HermesInternal
console.log('app js engine :', isHermes ? 'hermes' : 'jsc')

require('moment-timezone')

moment.tz.setDefault('Asia/Seoul')

// import { observer } from 'mobx-react-lite'

const Stack = createStackNavigator()

const codePushOptions = {
  checkFrequency: codePush.CheckFrequency.MANUAL,
}

console.log('prepare stack navigation')

const App = () => {

  Text.defaultProps = Text.defaultProps || {}
  Text.defaultProps.allowFontScaling = false

  useEffect(() => {

  }, [])

  return (
    <>
      <Text>aaa</Text>
    </>
  )
}

export default codePush(codePushOptions)(Sentry.wrap(App))
