import { View, Text, FlatList, TouchableOpacity, Image, StyleSheet } from 'react-native';
import { TmdbMovie, TmdbMulti, TmdbShow } from '../types/tmdb';

type Item = TmdbMovie | TmdbShow | TmdbMulti;

export default function CategoryRow({ title, items, onPress }: {
  title: string;
  items: Item[];
  onPress: (item: Item) => void;
}) {
  return (
    <View style={{ marginVertical: 12 }}>
      <Text style={styles.titleText}>{title}</Text>
      <FlatList
        data={items}
        horizontal
        keyExtractor={item => item.id.toString()}
        renderItem={({ item }) => (
          <TouchableOpacity onPress={() => onPress(item)} style={{ marginHorizontal: 6 }}>
            {item.poster_path && (
              <Image
                source={{ uri: `https://image.tmdb.org/t/p/w342${item.poster_path}` }}
                style={styles.poster}
              />
            )}
          </TouchableOpacity>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  titleText: { marginHorizontal: 16, fontSize: 20, fontWeight: 'bold', color: '#fff' },
  poster: { width: 120, height: 180, borderRadius: 8 },
});

