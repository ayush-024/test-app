import Home from './src/screens/Home';
import { SafeAreaProvider } from 'react-native-safe-area-context';

export default function App() {
  return (
    <SafeAreaProvider style={{ flex: 1, backgroundColor: '#000' }}>
      <Home />
    </SafeAreaProvider>
  );
}
