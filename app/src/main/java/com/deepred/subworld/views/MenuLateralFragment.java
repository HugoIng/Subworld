package com.deepred.subworld.views;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.deepred.subworld.R;

/**
 *
 */
public class MenuLateralFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_lateral, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
       /* app = (AplicatyApplication) getActivity().getApplication();
        workingUser =  LoggedUserAdapter.getInstance().getTheOnlyOne();
        filtrosFAS = new FiltrosFAS();
        btLogo = (CircleImageView) getActivity().findViewById(R.id.imPerfil);

        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);

        btLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), PerfilActivity.class);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(app.getCuttentActivity(), v, v.getTransitionName());
                    startActivity(i, options.toBundle());
                } else {
                    startActivity(i);
                }


            }
        });
        TextView nombreUsuario = (TextView) getActivity().findViewById(R.id.nombreUsuario);
        nombreUsuario.setText(workingUser.getUserName());

        TextView estadoUsuario = (TextView) getActivity().findViewById(R.id.estadoUsuario);
        estadoUsuario.setText(EMPTY_STRING + workingUser.getUserSmsState() + EMPTY_STRING);


        ProgressBar cuota = (ProgressBar) getActivity().findViewById(R.id.cuota);
        cuota.setMax(CUOTA_MAX);
        cuota.setProgress(workingUser.getCuota());
        int porcentajeCuota = workingUser.getCuota() * 100 / (20 * 1024 * 1024);
        TextView tvCuota = (TextView) getActivity().findViewById(R.id.textoCuota);
        tvCuota.setText((workingUser.getCuota() / (1024 * 1024)) + " Gb (" + porcentajeCuota + "%) " + getString(R.string.cuota));

        listMenuLateral=(ListView)getActivity().findViewById(R.id.lstMenuLateral);
        ArrayList<MenuLateralItem> ItemMenuLaterals = new ArrayList<>();
        ItemMenuLaterals.add(new MenuLateralItem(getResources().getString(R.string.profile),  R.drawable.ic_account_circle_black_36dp));
        ItemMenuLaterals.add(new MenuLateralItem(getResources().getString(R.string.datas),  R.drawable.ic_assignment_black_24dp));
        ItemMenuLaterals.add(new MenuLateralItem(getResources().getString(R.string.releases),  R.drawable.ic_description_black_36dp));
        ItemMenuLaterals.add(new MenuLateralItem(getResources().getString(R.string.drafts),  R.drawable.ic_hourglass_full_black_36dp));
        ItemMenuLaterals.add(new MenuLateralItem(getResources().getString(R.string.following),  R.drawable.ic_star_black_36dp));
        ItemMenuLaterals.add(new MenuLateralItem(getResources().getString(R.string.archivados),  R.drawable.ic_archive_black_24dp));
        ItemMenuLaterals.add(new MenuLateralItem(getResources().getString(R.string.settings),  R.drawable.ic_setting_light));
        ItemMenuLaterals.add(new MenuLateralItem(getResources().getString(R.string.close_season),  R.drawable.ic_power_settings_new_black_36dp));
        //ItemMenuLaterals.add(new MenuLateralItem(getResources().getString(R.string.chooser_title),  R.drawable.ic_folder));
*/

        /*menuAdapterAjustes = new MenuAdapterAjustes(this.getActivity(), ItemMenuLaterals);
        listMenuLateral.setAdapter(menuAdapterAjustes);
        listMenuLateral.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                switch (position) {
                    case 0:
                        startActivity(new Intent(getActivity().getApplicationContext(), PerfilActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(getActivity().getApplicationContext(), MisDatosActivity.class));
                        break;
                    case 2:
                        if (app.isConnected() && checkConnection(getActivity())) {
                            filtrosFAS.setTipo(BAG_MIS_PUBS);
                            app.setWorkingFiltrosFAS(filtrosFAS);
                            app.setIsItemsRecyclerEnable(false);
                            startActivity(new Intent(getActivity().getApplicationContext(), MisPublicacionesActivity.class));
                        } else {
                            AppDialog.showDialog(getActivity(), R.layout.custom_dialog, R.string.connection, R.string.no_connection_pub, false);
                        }
                        break;
                    case 3:
                        filtrosFAS.setTipo(BAG_MIS_BORRADORES);
                        app.setWorkingFiltrosFAS(filtrosFAS);
                        app.setIsItemsRecyclerEnable(false);
                        startActivity(new Intent(getActivity().getApplicationContext(), MisBorradoresActivity.class));
                        break;
                    case 4:
                        startActivity(new Intent(getActivity().getApplicationContext(), SiguiendoActivity.class));
                        break;
                    case 5:
                        startActivity(new Intent(getActivity().getApplicationContext(), MensajeriaArchivadaActivity.class));
                        break;
                    case 6:
                        // Display the fragment as the main content.
                        getFragmentManager().beginTransaction()
                                .replace(android.R.id.content, new PreferencesSoapbox())
                                .addToBackStack(null)
                                .commit();
                        break;
                    case 7:
                        app.setIdLastLoggedUser(workingUser.getId());
                        app.setIdLoggedUser(ZERO_LONG);
                        app.setImportContacts(true);
                        startActivity(new Intent(getActivity().getApplicationContext(), InitApplication.class));
                        getActivity().finish();

                        ServiceBoot serviceBoot = app.getServiceBoot();
                        if(serviceBoot != null) {
                            serviceBoot.disconnect();
                        }
                        break;
                }
            }
        });*/
    }

    @Override
    public void onResume() {
        super.onResume();
        //workingUser=LoggedUserAdapter.getInstance().getTheOnlyOne();
        //listMenuLateral.setAdapter(menuAdapterAjustes);

        /*try{
            btLogo.loadSmallAvatar(new Contacto(workingUser));
        }catch(Exception e){
            com.aplicaty.soapbox.util.LogClass.logError(ILogsTags.BITMAP, "Error setting a bitmap.", e);
        }*/

    }
}
