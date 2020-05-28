package com.ashu.myapplication.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.ashu.myapplication.R
import com.ashu.myapplication.R.layout.drawer_header
import com.ashu.myapplication.fragments.*
import com.ashu.myapplication.fragments.HomepageFragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var frameLayout: FrameLayout
    lateinit var toolBar: Toolbar
    lateinit var sharedPreferences: SharedPreferences
    lateinit var sharedPreferences2:SharedPreferences

    var previousMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigationView = findViewById(R.id.navigationView)
        drawerLayout = findViewById(R.id.drawerLayout)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        frameLayout = findViewById(R.id.frameLayout)
        toolBar = findViewById(R.id.toolBar)
        sharedPreferences = getSharedPreferences("Login Preferences", Context.MODE_PRIVATE)
        sharedPreferences2 = getSharedPreferences("Mobile Preferences",Context.MODE_PRIVATE)
        val name = sharedPreferences2.getString("user_name","Abcdefegh")
        val mobile = sharedPreferences2.getString("mobile","000000000")
        setUpToolBar()
        openHomePage()

        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this@MainActivity, drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener {
            if (previousMenuItem != null) {
                previousMenuItem?.isChecked = false
            }
            it.isCheckable = true
            it.isChecked = true
            previousMenuItem = it


            when (it.itemId) {
                R.id.homePage -> {
                    openHomePage()
                    drawerLayout.closeDrawers()
                }
                R.id.favourites -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, FavouritesFragment())
                        .commit()

                    supportActionBar?.title = "Favourites"
                    drawerLayout.closeDrawers()
                }
                R.id.orderHistory -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout,OrderHistoryFragment())
                        .commit()
                    supportActionBar?.title = "My Previous Orders"
                    drawerLayout.closeDrawers()
                }
                R.id.profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, ProfileFragment())
                        .commit()

                    supportActionBar?.title = "Profile"
                    drawerLayout.closeDrawers()
                }
                R.id.faq -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, FaqFragment())
                        .commit()

                    supportActionBar?.title = "FAQ"
                    drawerLayout.closeDrawers()
                }
                R.id.info -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, AppInfoFragment())
                        .commit()

                    supportActionBar?.title = "App Info"
                    drawerLayout.closeDrawers()
                }
                R.id.LogOut -> {
                    val dialog = AlertDialog.Builder(this)
                    dialog.setTitle("Confirmation!!")
                        .setMessage("Sure To Log Out ?")

                        .setPositiveButton("LogOut") { _, _ ->
                            sharedPreferences.edit().remove("isLoggedIn").apply()
                            val intent = Intent(
                                this@MainActivity,
                                LoginActivity::class.java
                            )
                            startActivity(intent)
                            Toast.makeText(
                                this@MainActivity,
                                "Logged Out Successfully",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            finish()
                        }
                        .setNegativeButton("Cancel") { _, _ ->

                        }
                        .create()
                        .show()
                }

            }
            return@setNavigationItemSelectedListener true
        }

        val changeView = LayoutInflater.from(this@MainActivity).inflate(drawer_header, null)
        val userName: TextView = changeView.findViewById(R.id.txtDrawerName)
        val userPhone: TextView = changeView.findViewById(R.id.txtDrawerMobile)
        val profileIcon: ImageView = changeView.findViewById(R.id.imgDrawerImage)
        userName.text =name
        userPhone.text = "+(91)-${mobile}"
        navigationView.addHeaderView(changeView)

        userName.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, ProfileFragment())
                .commit()

            supportActionBar?.title = "Profile"
            drawerLayout.closeDrawers()
        }
        profileIcon.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, ProfileFragment())
                .commit()

            supportActionBar?.title = "Profile"
            drawerLayout.closeDrawers()
        }



    }

    fun setUpToolBar() {
        setSupportActionBar(toolBar)
        supportActionBar?.title = "Hello User"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    fun openHomePage() {
        val fragment = HomepageFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout, fragment)
        transaction.commit()
        supportActionBar?.title = "Restaurants"
        navigationView.setCheckedItem(R.id.homePage)
    }

    override fun onBackPressed() {
        val frag = supportFragmentManager.findFragmentById(R.id.frameLayout)
        when (frag) {
            !is HomepageFragment -> openHomePage()

            else -> super.onBackPressed()
        }

    }


}

