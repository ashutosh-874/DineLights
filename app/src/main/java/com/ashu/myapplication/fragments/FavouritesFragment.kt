package com.ashu.myapplication.fragments

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room

import com.ashu.myapplication.R
import com.ashu.myapplication.adapter.FavouriteRecyclerAdapter
import com.ashu.myapplication.database.ResDatabase
import com.ashu.myapplication.database.ResEntity

class FavouritesFragment : Fragment() {

    lateinit var recyclerFavourite: RecyclerView
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: FavouriteRecyclerAdapter
    lateinit var zeroFav:RelativeLayout

    var dbResList = listOf<ResEntity>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =  inflater.inflate(R.layout.fragment_favourites, container, false)


        recyclerFavourite = view.findViewById(R.id.recyclerFavourite)
        progressLayout = view.findViewById(R.id.progressLayout1)
        progressBar = view.findViewById(R.id.progressBar1)
        layoutManager = GridLayoutManager(activity as Context, 2)
        dbResList = RetrieveFavourites(activity as Context).execute().get()
        zeroFav = view.findViewById(R.id.zeroFav)


        if (dbResList.isEmpty()){
            zeroFav.visibility = View.VISIBLE
        }
        else{
            zeroFav.visibility = View.GONE
        }

        if (activity != null) {
            progressLayout.visibility = View.GONE
            recyclerAdapter = FavouriteRecyclerAdapter(activity as Context, dbResList)
            recyclerFavourite.adapter =recyclerAdapter
            recyclerFavourite.layoutManager = layoutManager
        }

        return view

    }


    class RetrieveFavourites(val context: Context) : AsyncTask<Void, Void, List<ResEntity>>() {

        override fun doInBackground(vararg p0: Void?): List<ResEntity> {
            val db = Room.databaseBuilder(context, ResDatabase::class.java, "res-db").build()

            return db.resDao().getAllRestaurants()
        }

    }

}
