import core.board.ClientBoard
import core.board.ClientCell
import core.board.ClientAbility
import program.main.SceneUtil
import shared.board.Board
import shared.board.Cell
import shared.board.Ability
import core.graphics.scenes.BattleScene
import core.graphics.scenes.Scenes
import shared.other.DataUtil

def onCheck(Ability spell, Board board, Cell target){
    DataUtil.distance(spell.caster.position, target) <= 5
}

def onCheckAOE(Ability spell, Board board, Cell from, Cell to){
    DataUtil.distance(from, to) <= 3
}

def onCastBegin(ClientAbility spell, Board board, ClientCell target){
	spell.caster.setFacing(target);
	1.0F
}

def onCastEnd(Ability spell, ClientBoard board, Cell target){
    def units = board.units.findAll { it != spell.caster && DataUtil.distance(it.position, target) <= 3 }

    units.each {
        def to = SceneUtil.getScene(Scenes.BATTLE, BattleScene.class).getSpatialByUnit(it).node;
        board.addEffect("heal-effect", to)
    }

    board.addBuff("silenceBuff", 1, 6, units)
	board.nextTurn()
	spell.putOnCoolDown()
}