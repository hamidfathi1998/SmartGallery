package ir.hfathi.smart_gallery.injection

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import ir.hfathi.smart_gallery.feature_node.data.data_source.InternalDatabase
import ir.hfathi.smart_gallery.feature_node.data.repository.MediaRepositoryImpl
import ir.hfathi.smart_gallery.feature_node.domain.repository.MediaRepository
import ir.hfathi.smart_gallery.feature_node.domain.use_case.MediaUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideDatabase(app: Application): InternalDatabase {
        return Room.databaseBuilder(app, InternalDatabase::class.java, InternalDatabase.NAME)
            .build()
    }

    @Provides
    @Singleton
    fun provideMediaUseCases(repository: MediaRepository, @ApplicationContext context: Context): MediaUseCases {
        return MediaUseCases(context, repository)
    }

    @Provides
    @Singleton
    fun provideMediaRepository(
        @ApplicationContext context: Context,
        database: InternalDatabase
    ): MediaRepository {
        return MediaRepositoryImpl(context, database)
    }
}
