import { useEffect, useState } from 'react';
import { ScrollView, StatusBar, View } from 'react-native';
import BootSplash from '../components/BootSplash';
import HeroCarousel from '../components/HeroCarousel';
import CategoryRow from '../components/CategoryRow';
import { getPopularMovies, getPopularShows, searchTMDB } from '../providers/tmdb';
import { TmdbMovie, TmdbShow, TmdbMulti } from '../types/tmdb';

export default function Home() {
  const [bootDone, setBootDone] = useState(false);
  const [heroItems, setHeroItems] = useState<TmdbMovie[]>([]);
  const [trendingMovies, setTrendingMovies] = useState<TmdbMovie[]>([]);
  const [popularShows, setPopularShows] = useState<TmdbShow[]>([]);
  const [animeItems, setAnimeItems] = useState<TmdbMulti[]>([]);
  const [popularMovies, setPopularMovies] = useState<TmdbMovie[]>([]);

  useEffect(() => {
    getPopularMovies().then(data => {
      setHeroItems(data.slice(0, 5));
      setTrendingMovies(data);
      setPopularMovies(data);
    });
    getPopularShows().then(setPopularShows);
    searchTMDB('anime').then(setAnimeItems);
  }, []);

  if (!bootDone) return <BootSplash onDone={() => setBootDone(true)} />;

  return (
    <View style={{ flex: 1 }}>
      <StatusBar barStyle="light-content" />
      <ScrollView style={{ backgroundColor: '#000' }}>
        <HeroCarousel items={heroItems} />
        <CategoryRow title="Trending Movies" items={trendingMovies} onPress={() => {}} />
        <CategoryRow title="Popular Shows" items={popularShows} onPress={() => {}} />
        <CategoryRow title="Anime Hits" items={animeItems} onPress={() => {}} />
        <CategoryRow title="All-Time Popular" items={popularMovies} onPress={() => {}} />
      </ScrollView>
    </View>
  );
}
