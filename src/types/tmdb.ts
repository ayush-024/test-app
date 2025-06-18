export interface TmdbMovie {
  id: number;
  title: string;
  poster_path: string | null;
  backdrop_path?: string;
}

export interface TmdbShow {
  id: number;
  name: string;
  poster_path: string | null;
  backdrop_path?: string;
}

export interface TmdbMulti {
  id: number;
  media_type: 'movie' | 'tv' | 'person';
  title?: string;
  name?: string;
  poster_path: string | null;
  backdrop_path?: string;
}
