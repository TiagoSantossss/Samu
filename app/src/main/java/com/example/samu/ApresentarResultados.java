
package com.example.samu;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.ArrayList;

public class ApresentarResultados {

    private Context context;
    private LinearLayout linearLayout;
    private List<Resultado> resultados; // Sua lista de resultados

    public ApresentarResultados(Context context, LinearLayout linearLayout, List<Resultado> resultados) {
        this.context = context;
        this.linearLayout = linearLayout;
        this.resultados = resultados;
        apresentar();
    }

    private void apresentar() {
        linearLayout.removeAllViews(); // Limpar quaisquer views existentes

        for (Resultado resultado : resultados) {
            // Criar o layout para cada item (o retângulo)
            LinearLayout itemLayout = new LinearLayout(context);
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            itemParams.setMargins(8, 8, 8, 8);
            itemLayout.setLayoutParams(itemParams);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setBackgroundResource(android.R.drawable.list_selector_background);
            itemLayout.setPadding(16, 16, 16, 16);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);

            // Adicionar a logo
            ImageView logoImageView = new ImageView(context);
            LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(
                    80,
                    80
            );
            logoImageView.setLayoutParams(logoParams);

            // Definir a imagem com base no tipo
            String tipo = resultado.getTipo();
            if (tipo.equalsIgnoreCase("carro")) {
                logoImageView.setImageResource(R.drawable.ic_carro); // Crie este drawable
            } else if (tipo.equalsIgnoreCase("uber")) {
                logoImageView.setImageResource(R.drawable.ic_uber); // Crie este drawable
            } else if (tipo.equalsIgnoreCase("bolt")) {
                logoImageView.setImageResource(R.drawable.ic_bolt); // Crie este drawable
            } else if (tipo.equalsIgnoreCase("autocarro")) {
                logoImageView.setImageResource(R.drawable.ic_autocarro); // Crie este drawable
            } else if (tipo.equalsIgnoreCase("metro")) {
                logoImageView.setImageResource(R.drawable.ic_metro); // Crie este drawable
            } else if (tipo.equalsIgnoreCase("comboio")) {
                logoImageView.setImageResource(R.drawable.ic_comboio); // Crie este drawable
            } else if (tipo.equalsIgnoreCase("pé")) {
                logoImageView.setImageResource(R.drawable.ic_andar); // Crie este drawable
            } else {
                logoImageView.setImageResource(android.R.drawable.ic_menu_help); // Imagem padrão se o tipo não for reconhecido
            }
            itemLayout.addView(logoImageView);

            // Layout vertical para preço e tempo
            LinearLayout infoLayout = new LinearLayout(context);
            LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            );
            infoLayout.setLayoutParams(infoParams);
            infoLayout.setOrientation(LinearLayout.VERTICAL);
            infoLayout.setPadding(16, 0, 0, 0);

            // Adicionar o preço
            TextView priceTextView = new TextView(context);
            priceTextView.setText(String.valueOf(resultado.getPrice()));
            infoLayout.addView(priceTextView);

            // Adicionar o tempo
            TextView timeTextView = new TextView(context);
            timeTextView.setText(resultado.getTime());
            infoLayout.addView(timeTextView);

            itemLayout.addView(infoLayout);

            // Adicionar o botão "Ask"
            Button askButton = new Button(context);
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            askButton.setLayoutParams(buttonParams);
            askButton.setText("Ask");
            askButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Lógica para o botão "Ask"
                    // Você pode precisar acessar o objeto Resultado associado a este botão
                }
            });
            itemLayout.addView(askButton);

            // Adicionar o layout do item ao LinearLayout principal
            linearLayout.addView(itemLayout);
        }
    }

    // Classe de exemplo para representar um resultado
    public static class Resultado {
        private String tipo;
        private double price;
        private String time;

        public Resultado(String tipo, double price, String time) {
            this.tipo = tipo;
            this.price = price;
            this.time = time;
        }

        public String getTipo() {
            return tipo;
        }

        public double getPrice() {
            return price;
        }

        public String getTime() {
            return time;
        }
    }

    // Método de exemplo para criar alguns resultados (substitua pela sua lógica real)
    public static List<Resultado> criarResultadosDeExemplo(Context context) {
        List<Resultado> listaResultados = new ArrayList<>();
        listaResultados.add(new Resultado("carro", 19.99, "5 min"));
        listaResultados.add(new Resultado("uber", 25.50, "10 min"));
        listaResultados.add(new Resultado("bolt", 12.75, "7 min"));
        listaResultados.add(new Resultado("autocarro", 2.00, "15 min"));
        listaResultados.add(new Resultado("metro", 1.50, "8 min"));
        listaResultados.add(new Resultado("comboio", 5.00, "25 min"));
        listaResultados.add(new Resultado("pé", 0.00, "30 min"));
        return listaResultados;
    }
}