package commnet.test;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class testingPattern {

	public static void main(String[] args) {
		String string = "#632 Hello World #456fd kfireokfer fijeriufjer #13423432 fjirefjreifref Fixes #3."
				+ " Info: babel/babel#1018 test.com/#de"
				+ "[#395](http://www.devmedia.com.br/trabalhando-com-string-string-em-java/21737)\r\n\r\nhttps://github.com/christophercliff/flatmarket/pull/4"
				+ "Alright so almost there but not quite... So here we go:\r\n\r\nSteps so far to try to fool proof this:\r\nusing react-native-cli@2.0.1\r\n\r\n```\r\nreact-native init hello\r\ncd hello\r\nexport PKG=eslint-config-airbnb;\r\nnpm info \"$PKG@latest\" peerDependencies --json | command sed 's/[\\{\\},]//g ; s/: /@/g' | xargs npm install --save-dev \"$PKG@latest\"\r\ntouch .eslintrc\r\n```\r\n\r\nIn the .eslintrc:\r\n```\r\n{\r\n  \"extends\": \"airbnb\",\r\n  \"rules\": {\r\n    \"react/jsx-filename-extension\": [\"error\", { \"extensions\": [\".js\", \".jsx\"] }],\r\n  }\r\n}\r\n```\r\n\r\nThen when editing the `index.android.js` to:\r\n\r\n```\r\n/**\r\n * Sample React Native App\r\n * https://github.com/facebook/react-native\r\n * @flow\r\n */\r\n\r\nimport React from 'react';\r\nimport {\r\n  AppRegistry,\r\n  StyleSheet,\r\n  Text,\r\n  View,\r\n} from 'react-native';\r\n\r\nconst styles = StyleSheet.create({\r\n  container: {\r\n    flex: 1,\r\n    justifyContent: 'center',\r\n    alignItems: 'center',\r\n    backgroundColor: '#F5FCFF',\r\n  },\r\n  welcome: {\r\n    fontSize: 20,\r\n    textAlign: 'center',\r\n    margin: 10,\r\n  },\r\n  instructions: {\r\n    textAlign: 'center',\r\n    color: '#333333',\r\n    marginBottom: 5,\r\n  },\r\n});\r\n\r\nexport default class hello extends React.Component {\r\n  render() {\r\n    return (\r\n      <View style={styles.container}>\r\n        <Text style={styles.welcome}>\r\n          Welcome to React Native!\r\n        </Text>\r\n        <Text style={styles.instructions}>\r\n          To get started, edit index.android.js\r\n        </Text>\r\n        <Text style={styles.instructions}>\r\n          Double tap R on your keyboard to reload,{'\\n'}\r\n          Shake or press menu button for dev menu\r\n        </Text>\r\n      </View>\r\n    );\r\n  }\r\n}\r\n\r\nAppRegistry.registerComponent('hello', () => hello);\r\n```\r\n\r\nThen when running `./node_modules/.bin/eslint index.android.js` I get:\r\n\r\n```\r\n  34:16  error  Component should be written as a pure function  react/prefer-stateless-function\r\n```\r\n\r\nSo when I try to rewrite it into a pure function as such:\r\n\r\n```\r\n/**\r\n * Sample React Native App\r\n * https://github.com/facebook/react-native\r\n * @flow\r\n */\r\n\r\nimport React from 'react';\r\nimport {\r\n  AppRegistry,\r\n  StyleSheet,\r\n  Text,\r\n  View,\r\n} from 'react-native';\r\n\r\nconst styles = StyleSheet.create({\r\n  container: {\r\n    flex: 1,\r\n    justifyContent: 'center',\r\n    alignItems: 'center',\r\n    backgroundColor: '#F5FCFF',\r\n  },\r\n  welcome: {\r\n    fontSize: 20,\r\n    textAlign: 'center',\r\n    margin: 10,\r\n  },\r\n  instructions: {\r\n    textAlign: 'center',\r\n    color: '#333333',\r\n    marginBottom: 5,\r\n  },\r\n});\r\n\r\nconst hello = () => ({\r\n  return (\r\n    <View style={styles.container}>\r\n      <Text style={styles.welcome}>\r\n        Welcome to React Native!\r\n      </Text>\r\n      <Text style={styles.instructions}>\r\n        To get started, edit index.android.js\r\n      </Text>\r\n      <Text style={styles.instructions}>\r\n        Double tap R on your keyboard to reload,{'\\n'}\r\n        Shake or press menu button for dev menu\r\n      </Text>\r\n    </View>\r\n  );\r\n});\r\n\r\nexport default hello;\r\n\r\nAppRegistry.registerComponent('hello', () => hello);\r\n```\r\n\r\nand re-running `./node_modules/.bin/eslint index.android.js` I get:\r\n\r\n```\r\n36:5  error  Parsing error: Unexpected token <\r\n```\r\n\r\nAnd this is "
				+ "just #201705041534 using the default react-native lean template... Any ideas?";

		// String string = "#3333,\r #22
		// [#395](http://www.devmedia.com.br/trabalhando-com-string-string-em-java/21737)
		// "
		// + "```fdijdisj ``` sad ```#3456``` [dsd] sa [3244] ```\r\n36:5 error
		// Parsing error: tesantahd "
		// + "Unexpected token <\r\n```\r\n\r\n sjijre ";

		string = string.replaceAll("    .*?\n", "").replaceAll("    .*?\r", "").replaceAll("\r", "")
				.replaceAll("\n", "").replaceAll("```.*?```", "").replaceAll("``.*?``", "").replaceAll("`.*?`", "");

		ArrayList<String> relatedIssues = new ArrayList<>();
		ArrayList<String> relatedIssuesRefined = new ArrayList<>();
		String[] s = {};
		String auxString = "";

		Pattern pattern = Pattern.compile("#([0-9]+)");
		if (string.contains("/")) {
			s = string.split(" ");
		} else {
			auxString = string;
		}

		for (String i : s) {
			if (!i.contains("/")) {
				auxString = auxString + " " + i;
			}
		}

		Matcher m = pattern.matcher(auxString);

		while (m.find()) {
			relatedIssues.add(m.group());
		}

		for (String insda : relatedIssues) {
			if (insda.length() < 11) {
				relatedIssuesRefined.add(insda);
			}

			System.out.println(insda);
		}

		System.out.println("clean list");

		for (String test : relatedIssuesRefined) {
			System.out.println(test);
		}
	}

}
