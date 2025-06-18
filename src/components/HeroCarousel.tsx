import Carousel from 'react-native-snap-carousel';
import { Dimensions, ImageBackground, Text, StyleSheet } from 'react-native';
import { TmdbMovie } from '../types/tmdb';

export default function HeroCarousel({ items }: { items: TmdbMovie[] }) {
  const { width } = Dimensions.get('window');

  return (
    <Carousel
      data={items}
      sliderWidth={width}
      itemWidth={width}
      renderItem={({ item }) => (
        <ImageBackground source={{ uri: `https://image.tmdb.org/t/p/w780${item.backdrop_path}` }} style={styles.hero}>
          <Text style={styles.title}>{item.title}</Text>
        </ImageBackground>
      )}
    />
  );
}

const styles = StyleSheet.create({
  hero: { width: '100%', height: 220, justifyContent: 'flex-end', padding: 16 },
  title: { color: '#fff', fontSize: 28, fontWeight: 'bold', textShadowColor: '#000', textShadowRadius: 6 },
});
